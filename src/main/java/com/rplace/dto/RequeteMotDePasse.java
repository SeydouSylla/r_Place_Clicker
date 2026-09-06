package com.rplace.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// Nouveau DTO pour le changement de mot de passe
// On a fait un DTO separe de RequeteProfil parce que les regles de validation
// sont differentes : ici les deux champs sont obligatoires alors que dans
// RequeteProfil l'age est optionnel
// On demande deux fois le mot de passe pour eviter les fautes de frappe
public class RequeteMotDePasse {

    @NotBlank(message = "Le nouveau mot de passe est obligatoire")
    @Size(min = 4, message = "Le mot de passe doit faire au moins 4 caracteres")
    private String nouveauMotDePasse;

    @NotBlank(message = "La confirmation est obligatoire")
    private String confirmation;

    public String getNouveauMotDePasse() {
        return nouveauMotDePasse;
    }

    public void setNouveauMotDePasse(String n) {
        this.nouveauMotDePasse = n;
    }

    public String getConfirmation() {
        return confirmation;
    }

    public void setConfirmation(String c) {
        this.confirmation = c;
    }
}