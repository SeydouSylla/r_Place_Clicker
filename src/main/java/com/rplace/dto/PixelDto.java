package com.rplace.dto;

import com.rplace.modele.Pixel;

// Nous avons utilise ici DTO pour transferer les donnees d'un pixel vers le frontend.
// Cela evite d'exposer directement l'entite JPA Pixel au client.
// Nous avons aussi utilise le pattern Factory Method : depuisPixel() cree le DTO
// sans exposer l'entite. Ce pattern est documente ici :
// Nous avons utiliser les notions vues en cours et la source https://refactoring.guru/design-patterns/factory-method

public class PixelDto {
    private int x;
    private int y;
    private String couleur;
    private String auteurPseudo;
    private Long joueurId;
    private int prix;
    private int nbRecouvrements;

    // Factory Method : cree un PixelDto a partir d'une entite Pixel
    // On copie uniquement les donnees necessaires au frontend
    public static PixelDto depuisPixel(Pixel pixel) {
        PixelDto dto = new PixelDto();
        dto.x = pixel.getPositionX();
        dto.y = pixel.getPositionY();
        dto.couleur = pixel.getCouleur();
        dto.prix = pixel.getPrix();
        dto.nbRecouvrements = pixel.getNbRecouvrements();
        if (pixel.getJoueur() != null) {
            dto.auteurPseudo = pixel.getJoueur().getPseudo();
            dto.joueurId = pixel.getJoueur().getIdentifiant();
        }
        return dto;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public String getCouleur() {
        return couleur;
    }

    public String getAuteurPseudo() {
        return auteurPseudo;
    }

    public Long getJoueurId() {
        return joueurId;
    }

    public int getPrix() {
        return prix;
    }

    public int getNbRecouvrements() {
        return nbRecouvrements;
    }
}
