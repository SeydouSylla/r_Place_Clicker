# ADR-003 — Flyway pour un schéma versionné


## Contexte
Le schéma de base de données doit évoluer de façon contrôlée et reproductible
entre les environnements (local, staging, production).

## Décision
Gérer le schéma avec **Flyway**, en configurant Hibernate en
`ddl-auto=validate` : Hibernate ne modifie jamais le schéma, il valide
seulement qu'il correspond aux entités. Toute évolution passe par une migration
SQL versionnée dans `src/main/resources/db/migration`.

## Justification
- `ddl-auto=update` est pratique en développement mais dangereux en
  production : les modifications de schéma y sont implicites et non maîtrisées.
- Flyway rend chaque changement explicite, ordonné et rejouable à l'identique
  sur tous les environnements.
- La validation Hibernate garantit que le code et le schéma restent cohérents.

## Conséquences
- Chaque évolution du modèle nécessite une migration dédiée.
- Le démarrage de l'application applique automatiquement les migrations en
  attente avant la validation du schéma.
- Les tests d'intégration exécutent les migrations sur une base réelle
  (voir ADR-004).
