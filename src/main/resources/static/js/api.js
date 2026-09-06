// api.js - Toutes les communications avec le serveur passent par ce fichier
// On centralise les appels fetch() pour ne pas les repeter partout
// Source fetch API : https://developer.mozilla.org/fr/docs/Web/API/Fetch_API

// Fonction pour les requetes GET - recupere des donnees depuis le serveur
function apiGet(url) {
    return fetch(url, {
        method: 'GET',
        headers: { 'Accept': 'application/json' }
    }).then(function(rep) {
        if (!rep.ok) throw new Error('Erreur HTTP ' + rep.status);
        return rep.json();
    });
}

// Fonction pour les requetes POST - envoie des donnees au serveur
function apiPost(url, corps) {
    return fetch(url, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json',
                   'Accept': 'application/json' },
        body: corps ? JSON.stringify(corps) : null
    }).then(function(rep) {
        if (!rep.ok) {
            return rep.json().then(function(err) {
                throw new Error(err.message || 'Erreur ' + rep.status);
            });
        }
        return rep.json();
    });
}

// Charge le classement des 5 meilleurs joueurs par nombre de pixels poses
// On calcule le classement a partir des donnees de la grille
// Source Object.entries et sort :
// https://developer.mozilla.org/fr/docs/Web/JavaScript/Reference/Global_Objects/Object/entries
function chargerClassementRapide() {
    apiGet('/api/pixels').then(function(pixels) {
        // On compte le nombre de pixels par joueur
        var comptage = {};
        pixels.forEach(function(p) {
            if (p.auteurPseudo) {
                comptage[p.auteurPseudo] = (comptage[p.auteurPseudo] || 0) + 1;
            }
        });

        var conteneur = document.getElementById('classement');
        if (!conteneur) return;

        // On trie par nombre de pixels et on garde les 5 premiers
        var entrees = Object.entries(comptage)
            .sort(function(a, b) { return b[1] - a[1]; })
            .slice(0, 5);

        conteneur.innerHTML = '';
        if (entrees.length === 0) {
            conteneur.innerHTML =
                '<p class="vide">Aucun pixel pose encore.</p>';
            return;
        }

        // On affiche chaque joueur avec son rang et son nombre de pixels
        entrees.forEach(function(e, i) {
            var div = document.createElement('div');
            div.className = 'ligne-classement';
            div.innerHTML =
                '<span class="rang">' + (i + 1) + '</span>' +
                '<span class="pseudo">' + e[0] + '</span>' +
                '<span class="pixels">' + e[1] + ' px</span>';
            conteneur.appendChild(div);
        });
    }).catch(function(err) {
        console.error('Erreur classement :', err);
    });
}

// On charge le classement au demarrage et on le rafraichit toutes les 30 secondes
// Source setInterval : https://developer.mozilla.org/fr/docs/Web/API/setInterval
chargerClassementRapide();
setInterval(chargerClassementRapide, 30000);