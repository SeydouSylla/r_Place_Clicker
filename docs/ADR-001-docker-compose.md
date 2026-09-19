# ADR-001 — Docker Compose pour l'application


## Contexte
L'application r/place doit être déployée sur un serveur unique, avec deux
environnements (staging et production). Chaque environnement orchestre deux
conteneurs : l'application Spring Boot et sa base PostgreSQL.

## Décision
Déployer **l'application avec Docker Compose**, plutôt que de la faire tourner
dans un orchestrateur Kubernetes.

## Justification
- Le besoin de l'application se limite à quelques conteneurs sur une seule
  machine. Déployer r/place dans Kubernetes apporterait une complexité (objets,
  manifestes, contrôleurs) sans bénéfice proportionné à cette échelle.
- Docker Compose est simple à lire, à versionner et à faire évoluer.
- La montée en charge horizontale multi-nœuds n'est pas un objectif du projet.

## Conséquences
- Le déploiement de l'application reste léger : `docker compose pull` puis
  `up -d`.
- Pas de haute disponibilité automatique ni de montée en charge multi-nœuds
  pour l'application ; ce compromis est assumé au vu de l'objectif.
- Le HTTPS et le routage par domaine ne sont pas gérés par l'application
  elle-même mais au niveau de l'hôte (voir ADR-002). L'application se contente
  d'exposer son port local.
- La cohabitation de plusieurs environnements (et d'autres services) sur le même
  hôte demande une attention particulière aux ports et aux noms de conteneurs :
  chaque environnement expose un port local distinct, et les noms de conteneurs
  sont dérivés du nom du projet Compose pour éviter les collisions.
