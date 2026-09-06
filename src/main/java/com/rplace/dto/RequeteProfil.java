package com.rplace.dto;

import jakarta.validation.constraints.*;

// DTO pour la modification du profil (pseudo non modifiable).
public class RequeteProfil {
    @Min(value = 1, message = "Age invalide")
    @Max(value = 120, message = "Age invalide")
    private Integer age;

    @NotBlank(message = "Le pays est obligatoire")
    private String pays;

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
