// grille.js - Gestion de la grille 50x50 avec la Canvas API
// Source Canvas API : https://developer.mozilla.org/fr/docs/Web/API/Canvas_API

var TAILLE_PIXEL = 10; // chaque case fait 10x10 pixels a l'ecran
var NB_CASES = 50;     // grille de 50x50 = 2500 cases
var canvas = document.getElementById('grille');
var ctx = canvas ? canvas.getContext('2d') : null;

// Tableau local qui stocke l'etat de chaque pixel [x][y]
var grilleMemoire = [];

// Initialise le tableau avec des pixels blancs vierges
function initialiserGrille() {
    for (var x = 0; x < NB_CASES; x++) {
        grilleMemoire[x] = [];
        for (var y = 0; y < NB_CASES; y++) {
            grilleMemoire[x][y] = { couleur: '#FFFFFF', auteur: null, prix: 10 };
        }
    }
}

// Dessine un pixel sur le canvas a la position (x, y)
// Source fillRect : https://developer.mozilla.org/fr/docs/Web/API/CanvasRenderingContext2D/fillRect
function dessinerPixel(x, y, couleur) {
    if (!ctx) return;
    ctx.fillStyle = couleur || '#FFFFFF';
    ctx.fillRect(x * TAILLE_PIXEL, y * TAILLE_PIXEL,
                 TAILLE_PIXEL, TAILLE_PIXEL);
    // On dessine une bordure grise pour separer les cases
    ctx.strokeStyle = '#EEEEEE';
    ctx.lineWidth = 0.3;
    ctx.strokeRect(x * TAILLE_PIXEL, y * TAILLE_PIXEL,
                   TAILLE_PIXEL, TAILLE_PIXEL);
}

// Charge tous les pixels depuis l'API et les affiche sur la grille
function chargerGrille() {
    apiGet('/api/pixels').then(function(pixels) {
        pixels.forEach(function(p) {
            if (p.x >= 0 && p.x < NB_CASES && p.y >= 0 && p.y < NB_CASES) {
                grilleMemoire[p.x][p.y] = {
                    couleur: p.couleur || '#FFFFFF',
                    auteur: p.auteurPseudo || null,
                    prix: p.prix || 10
                };
                if (p.couleur) dessinerPixel(p.x, p.y, p.couleur);
            }
        });
    }).catch(function(e) { console.error('Erreur grille :', e); });
}

// Met a jour un pixel quand on recoit un message WebSocket
// Cette fonction est appelee depuis websocket.js
function mettreAJourPixel(pixelDto) {
    if (!pixelDto || pixelDto.x === undefined) return;
    grilleMemoire[pixelDto.x][pixelDto.y] = {
        couleur: pixelDto.couleur,
        auteur: pixelDto.auteurPseudo,
        prix: pixelDto.prix
    };
    dessinerPixel(pixelDto.x, pixelDto.y, pixelDto.couleur);
}

// Methode utilitaire qui pose UN pixel a la position donnee
// On a sorti ce code dans une fonction pour pouvoir l'appeler plusieurs fois
// quand on a le pinceau large (4 pixels par clic)
// On verifie qu'on ne sort pas de la grille pour eviter une erreur
function poserUnPixel(x, y, couleur) {
    if (x < 0 || x >= NB_CASES || y < 0 || y >= NB_CASES) return;

    // On dessine immediatement pour que le joueur voit son pixel tout de suite
    // sans attendre la reponse du serveur
    dessinerPixel(x, y, couleur);

    // On met a jour la memoire locale (utile pour le survol)
    grilleMemoire[x][y] = {
        couleur: couleur,
        auteur: 'Moi',
        prix: (grilleMemoire[x][y].prix || 10) + 5
    };

    // On envoie le pixel au serveur via WebSocket
    // (le serveur s'occupe du debit des credits et du broadcast aux autres joueurs)
    envoyerPixel(x, y, couleur);
}

if (canvas) {

    // Quand le joueur clique sur la grille on pose 1 ou 4 pixels
    // Source addEventListener : https://developer.mozilla.org/fr/docs/Web/API/EventTarget/addEventListener
    canvas.addEventListener('click', function(e) {
        if (!estConnecte()) {
            afficherNotification('Connectez-vous pour poser des pixels !', 'erreur');
            return;
        }

        // On calcule la case cliquee a partir de la position du clic
        var rect = canvas.getBoundingClientRect();
        var xClic = Math.floor((e.clientX - rect.left) / TAILLE_PIXEL);
        var yClic = Math.floor((e.clientY - rect.top) / TAILLE_PIXEL);

        if (xClic < 0 || xClic >= NB_CASES || yClic < 0 || yClic >= NB_CASES) return;

        var selecteur = document.getElementById('couleurChoisie');
        var couleur = selecteur ? selecteur.value : '#FF0000';

        // Si le joueur possede le bonus Pinceau large on pose un carre 2x2 (4 pixels)
        // Sinon on pose juste le pixel sur lequel il a clique
        // APinceauLarge est defini et mis a jour dans cliqueur.js
        // Source Array methods :
        // https://developer.mozilla.org/fr/docs/Web/JavaScript/Reference/Global_Objects/Array
        var aLePinceau = (typeof APinceauLarge !== 'undefined' && APinceauLarge);
        var positions = aLePinceau
            ? [[xClic, yClic], [xClic+1, yClic],
               [xClic, yClic+1], [xClic+1, yClic+1]]
            : [[xClic, yClic]];

        // Pour chaque position on appelle poserUnPixel qui gere
        // l'affichage local et l'envoi WebSocket au serveur
        // poserUnPixel ignore automatiquement les positions hors grille
        // (utile au bord de la grille avec le pinceau large)
        positions.forEach(function(pos) {
            poserUnPixel(pos[0], pos[1], couleur);
        });
    });

    // Quand la souris survole la grille on affiche les infos du pixel
    canvas.addEventListener('mousemove', function(e) {
        var rect = canvas.getBoundingClientRect();
        var x = Math.floor((e.clientX - rect.left) / TAILLE_PIXEL);
        var y = Math.floor((e.clientY - rect.top) / TAILLE_PIXEL);
        if (x < 0 || x >= NB_CASES || y < 0 || y >= NB_CASES) return;

        var infoDiv = document.getElementById('info-pixel');
        var auteurSpan = document.getElementById('auteur-pixel');
        var prixSpan = document.getElementById('prix-pixel');
        if (!infoDiv || !auteurSpan) return;

        // On affiche l'auteur et le prix du pixel survole
        var info = grilleMemoire[x][y];
        var texte = '(' + x + ',' + y + ') — ';
        texte += info.auteur ? info.auteur + ' — ' + info.prix + ' credits'
                             : 'Vierge — 10 credits';

        if (prixSpan && estConnecte()) {
            prixSpan.textContent = 'Prix : ' + (info.prix || 10) + ' credits';
        }

        auteurSpan.textContent = texte;
        infoDiv.style.display = 'block';
        infoDiv.style.left = (e.clientX + 15) + 'px';
        infoDiv.style.top  = (e.clientY + 15) + 'px';
    });

    // On cache la bulle d'info quand la souris quitte la grille
    canvas.addEventListener('mouseleave', function() {
        var d = document.getElementById('info-pixel');
        if (d) d.style.display = 'none';
    });

    initialiserGrille();
    chargerGrille();
}