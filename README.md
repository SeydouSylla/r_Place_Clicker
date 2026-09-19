# r/Place Clicker

Jeu web multijoueur en temps réel qui mêle deux idées : une **grille de pixels
partagée** à la *r/Place* (chacun colorie les cases d'un canevas commun) et une
mécanique d'***idle / incremental game*** à la *Cookie Clicker* (on accumule une
monnaie, on achète des générateurs qui en produisent toujours plus). Les
modifications de la grille sont diffusées **en direct** à tous les joueurs
connectés.

Le projet met l'accent autant sur l'application que sur sa **chaîne de livraison
complète** : de l'intégration continue jusqu'au déploiement automatisé en
production, en HTTPS, sur un serveur unique.

**En ligne :** [rplace.ssylla.com](https://rplace.ssylla.com) ·
staging : [staging.rplace.ssylla.com](https://staging.rplace.ssylla.com)

---

## Comment jouer

Le principe : poser des pixels de couleur sur une **grille partagée de 50×50**
(2500 cases), visible et modifiée par tout le monde en temps réel. Poser un
pixel coûte des **crédits**, la monnaie du jeu.

- **Au départ**, chaque joueur reçoit 50 crédits.
- **Poser un pixel** consomme des crédits ; la grille se met à jour
  instantanément chez tous les joueurs connectés, sans rechargement de page.
- **Gagner des crédits** de deux façons :
  - **le minage manuel** : cliquer pour produire des crédits à la main ;
  - **la production passive** : acheter des *générateurs* qui produisent des
    crédits automatiquement au fil du temps, même sans cliquer.
- **Les générateurs** se débloquent par paliers de prix croissants, chacun
  produisant plus que le précédent : Curseur, Grand-mère, Ferme, Mine, Usine.
  Plus on investit, plus la production augmente — le cœur de la boucle *idle*.
- **Les bonus de capacité** (par exemple le *Pinceau large*) améliorent la façon
  de jouer plutôt que la production.

Un **visiteur non connecté** peut voir la grille évoluer en temps réel et
consulter les statistiques et le classement, mais doit créer un compte pour
interagir (poser des pixels, miner, acheter des bonus).

---

## Fonctionnalités implémentées

**Jeu**
- Grille partagée de 50×50 (2500 pixels), initialisée au premier démarrage.
- Pose de pixels payante en crédits, avec vérification du solde.
- Diffusion temps réel des changements de la grille via WebSocket : chaque pixel
  posé apparaît immédiatement chez tous les joueurs connectés.
- Minage manuel de crédits.
- Régénération automatique des crédits dans le temps (tâche planifiée côté
  serveur).
- Boutique de bonus avec deux catégories : *générateurs* (production passive
  croissante) et *capacités* (amélioration du jeu).
- Historique des pixels posés (qui a posé quoi, et où).

**Comptes et profils**
- Inscription et connexion sécurisées (mots de passe hachés en bcrypt).
- Profil joueur : pseudo, âge, pays, date d'inscription.
- Modification du profil et changement de mot de passe.

**Consultation**
- Statistiques et classement des joueurs, accessibles même sans compte.
- Page de règles du jeu.

---

## Aperçu technique

| Domaine | Technologies |
|---|---|
| **Backend** | Spring Boot 4.1, Java 21 |
| **Temps réel** | Spring WebSocket (STOMP + SockJS) |
| **Sécurité** | Spring Security (BCrypt), sessions authentifiées |
| **Vues** | Thymeleaf (rendu serveur) |
| **Persistance** | Spring Data JPA, PostgreSQL 16 |
| **Migrations** | Flyway (schéma versionné) |
| **Conteneurisation** | Docker (build multi-stage), Docker Compose |
| **Routage / Ingress** | k3s (ingress Traefik) — point d'entrée HTTPS de l'hôte |
| **HTTPS / TLS** | Ingress k3s + cert-manager (Let's Encrypt) au niveau de l'hôte |
| **CI/CD** | GitHub Actions (test → build → publication GHCR → déploiement SSH) |
| **Supervision** | Spring Actuator (`/actuator/health`) |

---

## Architecture applicative

Monolithe Spring Boot organisé en couches :

```
controleur/    Points d'entrée HTTP (MVC) et REST
service/       Logique métier (pixels, crédits, bonus, statistiques)
repository/    Accès aux données (Spring Data JPA)
modele/        Entités JPA (Joueur, Pixel, HistoriquePixel, TypeBonus, JoueurBonus)
websocket/     Diffusion temps réel de la grille
securite/      Authentification et détails utilisateur
config/        Configuration Spring (sécurité, WebSocket)
scheduleur/    Tâches planifiées (régénération des crédits)
enumeration/   Catégories de bonus (générateur, capacité)
dto/           Objets de transfert (formulaires, réponses)
```

Le schéma de base est géré par **Flyway** (`ddl-auto=validate`) : Hibernate ne
modifie jamais le schéma, il valide seulement qu'il correspond aux entités.
Toute évolution passe par une migration versionnée.

---

## Architecture de déploiement

```
                 (git push)
Développeur ───────────────────►  GitHub
                                     │
                                     ▼
                          GitHub Actions (CI/CD)
                   ┌─────────────┬───────────────┬──────────────┐
                   │   test      │ build & push  │   deploy     │
                   │ (Test-      │   image →     │  SSH → VPS   │
                   │ containers) │    GHCR       │              │
                   └─────────────┴───────┬───────┴──────┬───────┘
                                         │              │
                              (image)    ▼              ▼ (docker compose pull/up)
                                       GHCR ─────────► VPS (Debian)
                                                        │
                                        ┌───────────────┴───────────────┐
                                        ▼                               ▼
                                       app                         PostgreSQL
                                  (Spring Boot,                 (volume persistant)
                                Docker Compose)

           Le HTTPS public est assuré au niveau de l'hôte : l'ingress de k3s
           (avec cert-manager pour Let's Encrypt) termine le TLS et route chaque
           domaine vers le conteneur applicatif Docker. r/place ne tourne pas
           dans k3s : k3s ne sert que de point d'entrée. Voir deploy/ et l'ADR-006.
```

- **`develop`** déploie automatiquement l'environnement *staging*
  (`staging.rplace.ssylla.com`).
- **`main`** déploie automatiquement la *production* (`rplace.ssylla.com`).

---

## Choix d'ingénierie

Les décisions structurantes sont documentées sous forme d'ADR (*Architecture
Decision Records*) dans [`docs/`](docs/) :

- **[ADR-001](docs/ADR-001-docker-compose.md)** — Docker Compose pour déployer
  l'application : orchestration proportionnée à un serveur unique.
- **[ADR-002](docs/ADR-002-https-hote.md)** — HTTPS géré au niveau de l'hôte
  (terminaison TLS et routage par domaine), l'application restant focalisée sur
  sa logique métier.
- **[ADR-003](docs/ADR-003-flyway.md)** — Flyway pour un schéma de base
  versionné et contrôlé (abandon de `ddl-auto=update`).
- **[ADR-004](docs/ADR-004-cicd-ssh.md)** — Déploiement continu par SSH et
  tests d'intégration avec Testcontainers.
- **[ADR-005](docs/ADR-005-gestion-secrets.md)** — Externalisation des secrets :
  aucune donnée sensible dans le dépôt.
- **[ADR-006](docs/ADR-006-k3s-point-entree-https.md)** — k3s comme point
  d'entrée HTTPS de l'hôte : r/place (en Docker Compose) s'appuie sur l'ingress
  k3s et cert-manager pour le TLS, sans tourner dans Kubernetes.

Ces documents expliquent le *pourquoi* de chaque choix, ses conséquences et les
évolutions envisagées.

---

## Sécurité

- **Aucun secret dans le dépôt.** Les identifiants de base de données et autres
  paramètres sensibles sont fournis par variables d'environnement ; seuls des
  modèles (`.env.example`) sont versionnés.
- **Clé de déploiement dédiée.** Le pipeline se connecte au serveur avec une clé
  SSH propre, révocable indépendamment.
- **HTTPS de bout en bout**, certificats renouvelés automatiquement.
- **Mots de passe hachés** (bcrypt).

---

## Lancer en local

Prérequis : Java 21, Docker.

```bash
# Copier le modèle d'environnement et renseigner les valeurs
cp .env.example .env

# Démarrer la base et l'application
docker compose up --build app database
```

L'application est alors accessible sur `http://localhost:8080`.

---

## Tests

```bash
./mvnw test
```

Le test d'intégration démarre une **vraie base PostgreSQL** dans un conteneur
via Testcontainers, y applique les migrations Flyway, et vérifie que le contexte
Spring démarre correctement. Docker doit donc être disponible pour lancer les
tests.

---

## Structure du dépôt

```
src/main/java/com/rplace/    Code applicatif (contrôleurs, services, modèles, WebSocket)
src/main/resources/
  ├── db/migration/          Migrations Flyway (V1__…)
  ├── templates/             Vues Thymeleaf
  └── static/                CSS et JavaScript (grille, WebSocket, cliqueur)
deploy/                      Configuration de déploiement (routage staging/production)
docker-compose.yaml          Développement local (build depuis les sources)
docker-compose.prod.yaml     Staging / production (image depuis GHCR)
.env.example                 Modèle de configuration pour le développement local
.env.prod.example            Modèle de configuration pour le serveur
.github/workflows/           Pipeline CI/CD
docs/                        Décisions d'architecture (ADR)
Dockerfile                   Build multi-stage (Maven → JRE Alpine)
```

---

## Auteurs

Projet personnel réalisé par **Seydou SYLLA** et **Hamidou NDIAYE**.

Au-delà de l'application elle-même, l'objectif était de mettre en pratique une
**démarche DevOps de bout en bout** : conteneurisation, intégration et
déploiement continus, gestion de plusieurs environnements, HTTPS automatisé et
externalisation des secrets — sur une infrastructure réelle, en conditions de
production.
