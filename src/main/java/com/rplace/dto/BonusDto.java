package com.rplace.dto;

import com.rplace.modele.TypeBonus;

// DTO pour transferer les donnees d'un bonus vers le frontend
// Contient aussi la quantite possedee par le joueur connecte
// On utilise le meme pattern Factory Method que PixelDto
// Source pattern Factory Method : https://refactoring.guru/design-patterns/factory-method
public class BonusDto {

    private Long identifiant;
    private String nom;
    private String categorie;
    private String description;
    private double multiplicateur;
    private int prix;
    private int quantitePossedee;

    // Cree un BonusDto a partir d'un TypeBonus et de la quantite possedee
    // Le prix est calcule selon la quantite deja possedee car il augmente
    // de 15% a chaque achat supplementaire comme dans Cookie Clicker
    public static BonusDto depuisTypeBonus(TypeBonus typeBonus, int quantitePossedee) {
        BonusDto dto = new BonusDto();
        dto.identifiant = typeBonus.getIdentifiant();
        dto.nom = typeBonus.getNom();
        dto.categorie = typeBonus.getCategorie().name();
        dto.description = typeBonus.getDescription();
        dto.multiplicateur = typeBonus.getMultiplicateur();
        // Le prix augmente selon la quantite deja possedee
        dto.prix = typeBonus.getPrixPourQuantite(quantitePossedee);
        dto.quantitePossedee = quantitePossedee;
        return dto;
    }

    public Long getIdentifiant() {
        return identifiant;
    }

    public String getNom() {
        return nom;
    }

    public String getCategorie() {
        return categorie;
    }

    public String getDescription() {
        return description;
    }

    public double getMultiplicateur() {
        return multiplicateur;
    }

    public int getPrix() {
        return prix;
    }

    public int getQuantitePossedee() {
        return quantitePossedee;
    }
}