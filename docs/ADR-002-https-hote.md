# ADR-002 — HTTPS géré au niveau de l'hôte


## Contexte
L'application doit être exposée en HTTPS sur deux domaines
(`staging.rplace.ssylla.com` et `rplace.ssylla.com`), avec des certificats
Let's Encrypt renouvelés automatiquement. Le serveur hôte fait déjà tourner un
point d'entrée web (l'ingress de k3s) qui occupe les ports 80 et 443 et gère les
certificats via cert-manager.

## Décision
Ne pas embarquer de reverse proxy dans le déploiement de l'application.
La **terminaison TLS et le routage par domaine sont assurés au niveau de
l'hôte** : l'application expose son port en local, et le point d'entrée de
l'hôte route chaque domaine vers le conteneur correspondant.

Le rôle précis de k3s dans ce routage, et la façon dont r/place s'y branche,
sont détaillés dans l'ADR-006.

## Justification
- Deux services ne peuvent pas écouter simultanément sur le port 443. Puisqu'un
  point d'entrée web existe déjà sur l'hôte, l'application passe derrière lui
  plutôt que d'ouvrir un second frontal (ce qui serait impossible sur les mêmes
  ports).
- La gestion des certificats est centralisée à un seul endroit sur la machine.
- L'application reste focalisée sur sa logique métier ; elle ne porte pas la
  responsabilité du TLS.

## Conséquences
- Chaque environnement expose l'application sur un port local distinct pour
  cohabiter sans conflit sur le même hôte.
- Les manifestes de routage sont versionnés dans `deploy/` (voir ADR-006).
- L'application n'a aucune dépendance à un reverse proxy particulier : elle
  fonctionne derrière n'importe quel frontal capable de router vers son port.
