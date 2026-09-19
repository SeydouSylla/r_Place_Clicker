# ADR-006 — k3s comme point d'entrée HTTPS de l'hôte


## Contexte
L'application r/place est déployée en Docker Compose (voir ADR-001) et doit être
exposée en HTTPS sur deux domaines, avec des certificats Let's Encrypt
renouvelés automatiquement (voir ADR-002).

Le serveur hôte fait tourner **k3s**, une distribution légère de Kubernetes.
k3s embarque un contrôleur d'ingress (Traefik) qui occupe les ports 80 et 443,
et **cert-manager**, qui obtient et renouvelle les certificats Let's Encrypt.
Ce point d'entrée HTTPS est déjà en place et opérationnel sur la machine.

## Décision
Ne pas ajouter de second reverse proxy pour r/place. L'application s'appuie sur
le **point d'entrée HTTPS existant de k3s** : elle expose son port en local, et
on déclare dans k3s les ressources qui routent son domaine vers ce port.

Concrètement, pour chaque environnement :

- un **Service + Endpoints** pointe vers le conteneur Docker de r/place, joint
  par l'adresse de la passerelle Docker de l'hôte (`172.17.0.1:<port>`) ;
- un **Ingress** route le domaine (`staging.rplace.ssylla.com` ou
  `rplace.ssylla.com`) vers ce Service, et déclenche la génération du certificat
  par cert-manager.

Le chemin du trafic est donc :

```
Internet (443)
   -> Traefik (ingress k3s) : terminaison TLS, certificat via cert-manager
   -> Service + Endpoints
   -> 172.17.0.1:<port local>
   -> conteneur Docker r/place
```

## Justification
- Les ports 80 et 443 ne peuvent être tenus que par un seul service. Puisque
  k3s les occupe déjà et gère les certificats, ouvrir un second frontal pour
  r/place serait impossible sur les mêmes ports et redondant.
- La terminaison TLS et le renouvellement des certificats sont ainsi
  centralisés à un seul endroit sur la machine, déjà éprouvé.
- r/place reste déployé en Docker Compose (ADR-001) : k3s ne sert qu'au routage
  d'entrée, l'application ne tourne pas dans Kubernetes. Les deux approches
  cohabitent, chacune dans son rôle.

## Conséquences
- Les ressources de routage k3s (Service, Endpoints, Ingress) sont versionnées
  dans `deploy/k3s/` (staging) et `deploy/k3s-production/` (production), avec un
  namespace, un domaine et un port distincts par environnement.
- Le port local exposé par chaque conteneur (`8090` en staging, `8091` en
  production) est bloqué en accès externe par le pare-feu : seul k3s, en local,
  le joint.
- L'application dépend de la présence de ce point d'entrée sur l'hôte. Sur un
  serveur sans point d'entrée existant, r/place pourrait à l'inverse embarquer
  son propre reverse proxy — le déploiement Docker Compose resterait identique,
  seul le routage d'entrée changerait.
