package com.rplace.dto;

import jakarta.validation.constraints.*;

// DTO pour le formulaire d'inscription avec validations.
public class RequeteInscription {
    @NotBlank(message = "Le pseudo est obligatoire")
    @Size(min = 3, max = 20, message = "Le pseudo : 3 a 20 caracteres")
    private String pseudo;

    @NotBlank(message = "Le mot de passe est obligatoire")
    @Size(min = 6, message = "Minimum 6 caracteres")
    private String motDePasse;

    @Min(value = 1, message = "Age invalide")
    @Max(value = 120, message = "Age invalide")
    private Integer age;

    @NotBlank(message = "Le pays est obligatoire")
    private String pays;

    public String getPseudo() {
        return pseudo;
    }

    public void setPseudo(String p) {
        this.pseudo = p;
    }

    public String getMotDePasse() {
        return motDePasse;
    }

    public void setMotDePasse(String m) {
        this.motDePasse = m;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer a) {
        this.age = a;
    }

    public String getPays() {
        return pays;
    }

    public void setPays(String p) {
        this.pays = p;
    }
}
