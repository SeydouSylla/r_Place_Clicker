package com.rplace.config;

import com.rplace.enumeration.CategorieBonus;
import com.rplace.modele.TypeBonus;
import com.rplace.repository.TypeBonusRepository;
import com.rplace.service.PixelService;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;


// Initialise les donnees au demarrage.
// @PostConstruct : execute une fois apres le demarrage de Spring.
// Cree la grille et le catalogue de bonus si absents.

@Component
public class InitialisationDonnees {

    private final PixelService pixelService;
    private final TypeBonusRepository typeBonusRepository;

    public InitialisationDonnees(PixelService pixelService,
                                 TypeBonusRepository typeBonusRepository) {
        this.pixelService = pixelService;
        this.typeBonusRepository = typeBonusRepository;
    }

    @PostConstruct
    public void initialiser() {
        pixelService.initialiserGrille();
        initialiserBonus();
    }

    // On cree le catalogue de bonus avec deux categories :
    //  - GENERATEUR : produit des credits passivement (curseur, grand-mere etc)
    //  - CAPACITE   : debloque une fonctionnalite de gameplay (pinceau large)
    // "couleurs supplementaires OU curseurs plus large"
    // On a choisi le pinceau large car ca apporte plus de valeur de gameplay
    // et c'est l'exemple precis donne dans l'enonce (4 pixels en un clic)
    private void initialiserBonus() {
        creerSiAbsent("Curseur", CategorieBonus.GENERATEUR, 100, 0.1,
                "Un curseur automatique qui clique pour vous");
        creerSiAbsent("Grand-mere", CategorieBonus.GENERATEUR, 500, 0.5,
                "Une grand-mere qui fabrique des credits avec amour");
        creerSiAbsent("Ferme", CategorieBonus.GENERATEUR, 2000, 2.0,
                "Une ferme qui cultive des credits en abondance");
        creerSiAbsent("Mine", CategorieBonus.GENERATEUR, 8000, 8.0,
                "Une mine qui extrait des credits en profondeur");
        creerSiAbsent("Usine", CategorieBonus.GENERATEUR, 30000, 30.0,
                "Une usine qui produit des credits industriellement");
        // Capacite cablee cote front (cf grille.js) : pose un carre 2x2 au clic
        creerSiAbsent("Pinceau large", CategorieBonus.CAPACITE, 5000, 0.0,
                "Posez 4 pixels d'un seul clic (carre 2x2)");
    }

    // Cree un bonus seulement s'il n'existe pas deja.
    private void creerSiAbsent(String nom, CategorieBonus categorie,
                               int prix, double multi, String desc) {
        if (!typeBonusRepository.existsByNom(nom)) {
            TypeBonus b = new TypeBonus();
            b.setNom(nom);
            b.setCategorie(categorie);
            b.setPrixBase(prix);
            b.setMultiplicateur(multi);
            b.setDescription(desc);
            typeBonusRepository.save(b);
        }
    }
}