package com.rplace.dto;

import com.rplace.modele.Joueur;

//DTO pour transferer les donnees d'un joueur.
// N'expose jamais le mot de passe hache (securite).

public class JoueurDto {
    private Long identifiant;
    private String pseudo;
    private Integer age;
    private String pays;
    private Long credits;
    private int nbPixelsEnPlace;
    private long nbPixelsTotalPoses;

    public static JoueurDto depuisJoueur(Joueur joueur) {
        JoueurDto dto = new JoueurDto();
        dto.identifiant = joueur.getIdentifiant();
        dto.pseudo = joueur.getPseudo();
        dto.age = joueur.getAge();
        dto.pays = joueur.getPays();
        dto.credits = joueur.getCredits();
        dto.nbPixelsEnPlace = joueur.getNbPixelsEnPlace();
        return dto;
    }

    public Long getIdentifiant() {
        return identifiant;
    }

    public String getPseudo() {
        return pseudo;
    }

    public Integer getAge() {
        return age;
    }

    public String getPays() {
        return pays;
    }

    public Long getCredits() {
        return credits;
    }

    public int getNbPixelsEnPlace() {
        return nbPixelsEnPlace;
    }

    public long getNbPixelsTotalPoses() {
        return nbPixelsTotalPoses;
    }

    public void setNbPixelsTotalPoses(long n) {
        this.nbPixelsTotalPoses = n;
    }
}
