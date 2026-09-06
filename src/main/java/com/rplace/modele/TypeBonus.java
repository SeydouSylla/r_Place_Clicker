package com.rplace.modele;

import com.rplace.enumeration.CategorieBonus;
import jakarta.persistence.*;

//Catalogue des bonus disponibles a l'achat.
//Inspire de Cookie Clicker : curseurs, grand-meres, fermes...
//Donnees fixes inserees au demarrage par InitialisationDonnees.
@Entity
@Table(name = "type_bonus")
public class TypeBonus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long identifiant;

    @Column(name = "nom", nullable = false, unique = true, length = 50)
    private String nom;

    // Type du bonus : GENERATEUR ou CAPACITE
    @Enumerated(EnumType.STRING)
    @Column(name = "categorie", nullable = false)
    private CategorieBonus categorie;

    // Prix de base en credits
    @Column(name = "prix_base", nullable = false)
    private int prixBase;

    // Credits produits par seconde par unite (0 pour CAPACITE)
    @Column(name = "multiplicateur", nullable = false)
    private double multiplicateur;

    @Column(name = "description", length = 200)
    private String description;

    // Methodes metier

    //Constructeurs
    public TypeBonus() {
    }

    //Prix pour acheter une unite supplementaire.
    //Augmente de 15% par unite deja possedee (comme Cookie Clicker original).
    public int getPrixPourQuantite(int quantiteActuelle) {
        return (int) (prixBase * Math.pow(1.15, quantiteActuelle));
    }

    //Getters et Setters
    public Long getIdentifiant() {
        return identifiant;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String n) {
        this.nom = n;
    }

    public CategorieBonus getCategorie() {
        return categorie;
    }

    public void setCategorie(CategorieBonus c) {
        this.categorie = c;
    }

    public int getPrixBase() {
        return prixBase;
    }

    public void setPrixBase(int p) {
        this.prixBase = p;
    }

    public double getMultiplicateur() {
        return multiplicateur;
    }

    public void setMultiplicateur(double m) {
        this.multiplicateur = m;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String d) {
        this.description = d;
    }
}
