# ADR-005 : Gestion des secrets et externalisation de la configuration sensible


## Contexte
Une application connectée à une base de données manipule des informations
sensibles : identifiants de connexion, mots de passe. Ces valeurs ne doivent
jamais se retrouver en clair dans le dépôt de code, où elles seraient visibles
par quiconque y a accès et conservées indéfiniment dans l'historique Git. La
configuration doit par ailleurs différer entre les environnements
(développement, staging, production) sans dupliquer ni exposer de secret.

## Décision
Toute la configuration sensible est fournie par variables d'environnement.
Le code ne contient aucune valeur secrète : `application.properties` lit les
identifiants via des variables (`${SPRING_DATASOURCE_PASSWORD}`, etc.), avec des
valeurs par défaut réservées au développement local. En production, ces
variables proviennent d'un fichier `.env` présent uniquement sur le serveur et
jamais versionné. Seuls des modèles documentaires (`.env.example`) figurent dans
le dépôt. Le pipeline de déploiement, quant à lui, s'authentifie auprès du
serveur via une clé SSH dédiée stockée dans les secrets du dépôt, hors du code.

## Justification
- **Aucun secret dans le contrôle de version.** Un secret committé est
  considéré comme compromis de façon permanente, car il subsiste dans
  l'historique même après correction. La seule prévention fiable est de ne
  jamais l'y placer.
- **Séparation configuration / code.** Les mêmes artefacts (image, code) sont
  déployés dans tous les environnements ; seule la configuration change, fournie
  au moment de l'exécution. C'est le principe de configuration par
  l'environnement, standard pour les applications déployées en conteneur.
- **Modèles explicites.** Les fichiers `.env.example` documentent les variables
  attendues sans divulguer de valeur, ce qui facilite la reprise du projet sans
  compromettre la sécurité.

## Conséquences
- Chaque environnement doit disposer de son fichier `.env` renseigné avec des
  valeurs propres (mots de passe distincts entre staging et production).
- Le fichier `.env` étant hors du dépôt, sa présence et son contenu sur le
  serveur relèvent d'une étape d'installation explicite, documentée.
- Un secret réellement exposé par le passé devrait être révoqué et remplacé, et
  pas seulement retiré du fichier : la correction du fichier ne suffit pas à
  elle seule pour un secret de production.

## Note sur l'historique du projet
Une version antérieure du fichier de configuration contenait un mot de passe de
développement générique en clair. Il a été externalisé. Cette valeur n'ouvrait
l'accès à aucune ressource réelle ; elle ne constituait pas un secret de
production. Le comportement correct pour un secret de production aurait été, en
plus de l'externalisation, de le révoquer et d'en générer un nouveau.

## Évolution envisagée
| Évolution possible | Apport attendu | Condition de déclenchement |
|---|---|---|
| Gestionnaire de secrets dédié (Vault, secrets managés du fournisseur) | Stockage chiffré, rotation, audit d'accès | Multiplication des secrets ou des environnements |
| Analyse automatique de secrets dans le pipeline | Détection d'un secret committé par erreur | Renforcement de la chaîne d'intégration continue |
