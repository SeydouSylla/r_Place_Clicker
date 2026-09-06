// cliqueur.js - Gestion du Cookie Clicker : credits, bonus et generateurs
// C'est la partie du jeu inspiree de Cookie Clicker
// Source setInterval : https://developer.mozilla.org/fr/docs/Web/API/setInterval

// Nous rafraichissons les credits toutes les 5 secondes
var INTERVALLE_CREDITS = 5000;

// Variable globale qui indique si le joueur possede le bonus pinceau large
// On la met a jour a chaque fois qu'on recharge la liste des bonus
// grille.js lit ce flag pour savoir si on pose 1 ou 4 pixels au clic
var APinceauLarge = false;

// Recupere le solde actuel depuis le serveur et met a jour l'affichage
function mettreAJourAffichageCredits() {
    if (!estConnecte()) return;
    apiGet('/api/credits').then(function(d) {
        var span = document.getElementById('nb-credits');
        if (span) span.textContent = d.credits;
    }).catch(function(e) { console.error('Erreur credits :', e); });
}

// Minage manuel - envoie une requete POST pour gagner 1 credit
function miner() {
    apiPost('/api/credits/miner', null).then(function(d) {
        var span = document.getElementById('nb-credits');
        if (span) span.textContent = d.credits;
        // Petite animation sur le bouton pour le feedback visuel
        var btn = document.getElementById('bouton-miner');
        if (btn) {
            btn.classList.add('anime');
            setTimeout(function() { btn.classList.remove('anime'); }, 200);
        }
    }).catch(function(e) {
        afficherNotification('Erreur lors du minage', 'erreur');
    });
}

// Charge et affiche la liste des bonus disponibles a l'achat
// Source innerHTML : https://developer.mozilla.org/fr/docs/Web/API/Element/innerHTML
function chargerBonus() {
    if (!estConnecte()) return;
    apiGet('/api/bonus').then(function(bonusList) {

        // Mise a jour du flag pinceau large pour grille.js
        // On regarde si le joueur possede au moins 1 unite du Pinceau large
        // Source Array.some : https://developer.mozilla.org/fr/docs/Web/JavaScript/Reference/Global_Objects/Array/some
        APinceauLarge = bonusList.some(function(b) {
            return b.nom === 'Pinceau large' && b.quantitePossedee > 0;
        });

        var conteneur = document.getElementById('liste-bonus');
        if (!conteneur) return;
        conteneur.innerHTML = '';
        bonusList.forEach(function(bonus) {
            var div = document.createElement('div');
            div.className = 'carte-bonus';
            div.innerHTML =
                '<strong>' + bonus.nom + '</strong>' +
                '<span class="quantite-bonus">x' + bonus.quantitePossedee + '</span>' +
                '<p>' + bonus.description + '</p>' +
                '<div class="bonus-footer">' +
                  '<span class="prix-bonus">' + bonus.prix + ' credits</span>' +
                  '<button class="bouton-acheter" ' +
                    'onclick="acheterBonus(' + bonus.identifiant + ')">' +
                    'Acheter' +
                  '</button>' +
                '</div>';
            conteneur.appendChild(div);
        });
    }).catch(function(e) { console.error('Erreur bonus :', e); });
}

// Achete un bonus et rafraichit l'affichage
function acheterBonus(bonusId) {
    apiPost('/api/bonus/' + bonusId + '/acheter', null).then(function() {
        afficherNotification('Bonus achete !', 'succes');
        mettreAJourAffichageCredits();
        // chargerBonus recharge la liste ET met a jour le flag APinceauLarge
        // Donc des qu'on achete le pinceau large, le clic suivant pose 4 pixels
        chargerBonus();
    }).catch(function(e) {
        afficherNotification(e.message || 'Credits insuffisants', 'erreur');
    });
}

// On branche le bouton miner au clic
var btn = document.getElementById('bouton-miner');
if (btn) btn.addEventListener('click', miner);

// On demarre le rafraichissement automatique et on charge les bonus
if (estConnecte()) {
    setInterval(mettreAJourAffichageCredits, INTERVALLE_CREDITS);
    chargerBonus();
}