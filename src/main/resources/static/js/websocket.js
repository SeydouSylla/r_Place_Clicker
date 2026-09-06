// websocket.js - Connexion WebSocket STOMP pour la grille en temps reel
// Source STOMP over WebSocket : https://stomp-js.github.io/stomp-websocket/codo/extra/docs-src/Usage.md.html
// Source SockJS : https://github.com/sockjs/sockjs-client

var clientStomp = null;

// On etablit la connexion WebSocket au chargement de la page
function connecterWebSocket() {
    var socket = new SockJS('/ws');
    clientStomp = Stomp.over(socket);
    clientStomp.debug = null; // on desactive les logs pour ne pas surcharger la console

    clientStomp.connect({},
        function onConnecte() {
            console.log('WebSocket connecte');
            // On s'abonne au canal /topic/grille pour recevoir les pixels en temps reel
            // Quand un joueur pose un pixel le serveur l'envoie a tous les abonnes
            clientStomp.subscribe('/topic/grille', function(message) {
                var pixelDto = JSON.parse(message.body);
                // On met a jour la grille pour tous les joueurs connectes
                mettreAJourPixel(pixelDto);
                // Si c'est notre propre pixel on rafraichit notre solde de credits
                if (estConnecte() && pixelDto.joueurId === getJoueurId()) {
                    mettreAJourAffichageCredits();
                }
            });
        },
        function onErreur(err) {
            console.error('Erreur WebSocket :', err);
            // On essaie de se reconnecter automatiquement apres 3 secondes
            setTimeout(connecterWebSocket, 3000);
        }
    );
}

// Envoie un pixel au serveur qui le broadcast a tous les clients
function envoyerPixel(x, y, couleur) {
    if (!clientStomp || !clientStomp.connected) {
        afficherNotification('Connexion perdue, reconnexion...', 'erreur');
        return;
    }
    clientStomp.send('/app/pixel', {}, JSON.stringify({x:x, y:y, couleur:couleur}));
}

// On ferme proprement la connexion WebSocket quand le joueur quitte la page
// Source beforeunload : https://developer.mozilla.org/fr/docs/Web/API/Window/beforeunload_event
function deconnecterWebSocket() {
    if (clientStomp && clientStomp.connected) {
        clientStomp.disconnect(function() {
            console.log('WebSocket deconnecte proprement');
        });
    }
}

window.addEventListener('beforeunload', deconnecterWebSocket);
connecterWebSocket();