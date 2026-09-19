# ADR-004 — Déploiement continu par SSH et tests avec Testcontainers


## Contexte
Le projet vise une chaîne de livraison automatisée : tester, construire une
image, la publier, puis la déployer sur le serveur, à chaque évolution.

## Décision
- **CI/CD via GitHub Actions** : à chaque push, le pipeline exécute les tests,
  construit l'image Docker, la publie sur GitHub Container Registry (GHCR),
  puis se connecte au serveur en **SSH** pour y récupérer la nouvelle image et
  redémarrer les conteneurs. `develop` déploie staging, `main` déploie la
  production.
- **Tests d'intégration avec Testcontainers** : le contexte applicatif est
  testé contre une vraie base PostgreSQL démarrée dans un conteneur jetable.

## Justification
- Le déploiement par SSH est simple et ne nécessite d'exposer aucune interface
  d'administration sur le serveur ; l'accès se fait avec une clé dédiée,
  révocable indépendamment.
- Publier l'image sur un registre découple la construction du déploiement :
  le serveur ne compile rien, il récupère une image déjà testée.
- Testcontainers teste l'application dans des conditions proches de la
  production (vraie base, migrations Flyway appliquées), ce qui détecte des
  erreurs qu'une base en mémoire masquerait.

## Conséquences
- Les secrets du déploiement (hôte, utilisateur, clé SSH) sont stockés dans les
  secrets GitHub, jamais dans le dépôt.
- Docker doit être disponible dans l'environnement de test (CI et local).
- Un échec de test bloque la construction et le déploiement.
