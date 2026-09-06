package com.rplace.modele;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

//Entite JPA representant un joueur inscrit.
//Correspond a la table JOUEUR en base de donnees PostgreSQL.*/
@Entity
@Table(name = "joueur")
public class Joueur {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long identifiant;

    // Pseudo unique entre 3 et 20 caracteres
    @Column(name = "pseudo", unique = true, nullable = false, length = 20)
    @NotBlank(message = "Le pseudo est obligatoire")
    @Size(min = 3, max = 20, message = "Le pseudo doit faire entre 3 et 20 caracteres")
    private String pseudo;

    // Mot de passe hache avec bcrypt - jamais stocke en clair
    @Column(name = "mot_de_passe_hache", nullable = false)
    private String motDePasseHache;

    @Column(name = "age")
    @Min(value = 1, message = "L'age doit etre positif")
    @Max(value = 120, message = "L'age doit etre realiste")
    private Integer age;

    @Column(name = "pays", length = 60)
    private String pays;

    // Solde de credits disponibles (50 au depart)
    @Column(name = "credits", nullable = false)
    private Long credits = 50L;

    // Date d'inscription automatique
    @Column(name = "date_inscription", nullable = false)
    private LocalDateTime dateInscription = LocalDateTime.now();

    // Derniere activite pour le scheduler de regeneration passive
    @Column(name = "derniere_activite")
    private LocalDateTime derniereActivite = LocalDateTime.now();

    // Pixels actuellement en place sur la grille
    @OneToMany(mappedBy = "joueur", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Pixel> pixelsActifs = new ArrayList<>();

    // Historique complet des placements
    @OneToMany(mappedBy = "joueur", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<HistoriquePixel> historique = new ArrayList<>();

    // Bonus achetes par ce joueur
    @OneToMany(mappedBy = "joueur", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<JoueurBonus> bonusAchetes = new ArrayList<>();

    //Constructeurs
    public Joueur() {
    }

    public Joueur(String pseudo, String motDePasseHache, Integer age, String pays) {
        this.pseudo = pseudo;
        this.motDePasseHache = motDePasseHache;
        this.age = age;
        this.pays = pays;
    }

    //Retourne le nombre de pixels encore en place sur la grille.
    public int getNbPixelsEnPlace() {
        return pixelsActifs.size();
    }

    // Retourne le pixel le plus ancien encore en place.
    public Pixel getPixelLePlusAncien() {
        return pixelsActifs.stream()
                .min((p1, p2) -> p1.getDatePose().compareTo(p2.getDatePose()))
                .orElse(null);
    }

    //Getters et Setters
    public Long getIdentifiant() {
        return identifiant;
    }

    public String getPseudo() {
        return pseudo;
    }

    public void setPseudo(String p) {
        this.pseudo = p;
    }

    public String getMotDePasseHache() {
        return motDePasseHache;
    }

    public void setMotDePasseHache(String m) {
        this.motDePasseHache = m;
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

    public Long getCredits() {
        return credits;
    }

    public void setCredits(Long c) {
        this.credits = c;
    }

    public LocalDateTime getDateInscription() {
        return dateInscription;
    }

    public LocalDateTime getDerniereActivite() {
        return derniereActivite;
    }

    public void setDerniereActivite(LocalDateTime d) {
        this.derniereActivite = d;
    }

    public List<Pixel> getPixelsActifs() {
        return pixelsActifs;
    }

    public List<HistoriquePixel> getHistorique() {
        return historique;
    }

    public List<JoueurBonus> getBonusAchetes() {
        return bonusAchetes;
    }
}
