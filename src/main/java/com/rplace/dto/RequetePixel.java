package com.rplace.dto;

import jakarta.validation.constraints.*;

// C'est un objet qui représente "la demande de poser un pixel" envoyée par le client.
// Il contient trois informations - la position X, la position Y, et la couleur.
// Validation des donnees avec Jakarta Bean Validation :
// Nous avons utilise les notions vues en cours et la source https://jakarta.ee/specifications/bean-validation/3.0/

public class RequetePixel {
    @Min(0)
    @Max(49)
    private int x;

    @Min(0)
    @Max(49)
    private int y;

    // La couleur doit etre au format hexadecimal
    @NotBlank
    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$")
    private String couleur;

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public String getCouleur() {
        return couleur;
    }

    public void setCouleur(String c) {
        this.couleur = c;
    }
}
