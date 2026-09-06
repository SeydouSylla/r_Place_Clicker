// auth.js - Gestion de la session cote client
// JOUEUR_CONNECTE et JOUEUR_ID sont injectes par Thymeleaf dans jeu.html

// Verifie si le joueur est connecte
function estConnecte() {
    return typeof JOUEUR_CONNECTE !== 'undefined' && JOUEUR_CONNECTE === true;
}

// Retourne l'identifiant du joueur connecte ou null sinon
function getJoueurId() {
    return estConnecte() && typeof JOUEUR_ID !== 'undefined' ? JOUEUR_ID : null;
}

// Affiche une notification temporaire qui disparait apres 3 secondes
// Source setTimeout : https://developer.mozilla.org/fr/docs/Web/API/setTimeout
function afficherNotification(message, type) {
    var ancienne = document.getElementById('notification-temp');
    if (ancienne) ancienne.remove();

    var div = document.createElement('div');
    div.id = 'notification-temp';
    div.className = 'alerte alerte-' + type;
    div.textContent = message;
    document.body.appendChild(div);

    setTimeout(function() {
        if (div.parentNode) div.remove();
    }, 3000);
}