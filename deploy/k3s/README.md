# Routage k3s vers r/Place (Docker Compose)

Ce dossier contient les ressources Kubernetes (k3s) qui exposent
r/Place en HTTPS, **sans** que r/Place lui-meme tourne dans k3s.

## Contexte

Sur ce VPS, k3s occupe deja les ports 80 et 443 et gere les
certificats Let's Encrypt via cert-manager. Comme deux services ne
peuvent pas ecouter sur le meme port, r/Place ne peut pas prendre le
port 443 directement.

La solution : r/Place tourne en Docker Compose et expose son
application sur un port local du VPS ; k3s recoit le trafic HTTPS
public et le transmet a ce port local.

## Fonctionnement

r/Place expose son application sur le port local `8090` du VPS.
On declare ensuite dans k3s :

- un **Service + Endpoints** (`rplace-service.yaml`) qui pointe vers
  `172.17.0.1:8090` (la passerelle docker0, par laquelle les pods k3s
  joignent les conteneurs Docker de l'hote) ;
- un **Ingress** (`rplace-ingress.yaml`) qui route
  `staging.rplace.ssylla.com` vers ce Service, avec un certificat
  Let's Encrypt gere par cert-manager.

Le trafic suit donc ce chemin :

```
Internet (443)
   -> Traefik (ingress k3s, certificat via cert-manager)
   -> Service rplace-externe
   -> 172.17.0.1:8090
   -> conteneur Docker rplace-app
```

## Deploiement

Prerequis : r/Place tourne en Docker Compose et repond sur
`http://172.17.0.1:8090/actuator/health`.

```bash
kubectl apply -f rplace-service.yaml
kubectl apply -f rplace-ingress.yaml

# Verifier l'emission du certificat (READY doit passer a True) :
kubectl get certificate -n rplace-staging

# Tester :
curl -sI https://staging.rplace.ssylla.com   # doit renvoyer HTTP/2 200
```

## Securite

Le port `8090` ne doit pas etre accessible depuis Internet (seul k3s,
en local, doit le joindre). On le bloque au pare-feu :

```bash
sudo ufw deny 8090/tcp
```

## Production

Pour l'environnement de production, dupliquer ces deux fichiers en
remplacant :

- le namespace `rplace-staging` par `rplace-production` ;
- l'hote `staging.rplace.ssylla.com` par `rplace.ssylla.com` ;
- le port `8090` par celui du conteneur de production (ex. `8091`)
  si les deux environnements tournent sur le meme VPS.