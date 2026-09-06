package com.rplace.modele;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

//Entite JPA representant un pixel sur la grille 50x50.
//2500 pixels existent en base (initialises au demarrage).
//Formule prix : 10 + (nbRecouvrements x 5).
@Entity
@Table(name = "pixel",
        uniqueConstraints = @UniqueConstraint(columnNames = {"position_x", "position_y"}))
public class Pixel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long identifiant;

    // Position dans la grille (0 a 49)
    @Column(name = "position_x", nullable = false)
    private int positionX;

    @Column(name = "position_y", nullable = false)
    private int positionY;

    // Couleur hex (#RRGGBB), null si pixel vierge
    @Column(name = "couleur", length = 7)
    private String couleur;

    // Compteur de recouvrements — determine le prix
    @Column(name = "nb_recouvrements", nullable = false)
    private int nbRecouvrements = 0;

    // Proprietaire actuel (null si vierge)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "joueur_id")
    private Joueur joueur;

    @Column(name = "date_pose")
    private LocalDateTime datePose;

    @OneToMany(mappedBy = "pixel", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<HistoriquePixel> historique = new ArrayList<>();

    //Methodes metier

    //Prix actuel du pixel.
    //Formule : 10 credits de base + 5 par recouvrement.

    //Constructeurs
    public Pixel() {
    }

    public Pixel(int positionX, int positionY) {
        this.positionX = positionX;
        this.positionY = positionY;
    }

    public int getPrix() {
        return 10 + (nbRecouvrements * 5);
    }

    // Vrai si le pixel n'a jamais ete colorie.
    public boolean estVierge() {
        return couleur == null;
    }

    //Getters et Setters
    public Long getIdentifiant() {
        return identifiant;
    }

    public int getPositionX() {
        return positionX;
    }

    public int getPositionY() {
        return positionY;
    }

    public String getCouleur() {
        return couleur;
    }

    public void setCouleur(String c) {
        this.couleur = c;
    }

    public int getNbRecouvrements() {
        return nbRecouvrements;
    }

    public void setNbRecouvrements(int n) {
        this.nbRecouvrements = n;
    }

    public Joueur getJoueur() {
        return joueur;
    }

    public void setJoueur(Joueur j) {
        this.joueur = j;
    }

    public LocalDateTime getDatePose() {
        return datePose;
    }

    public void setDatePose(LocalDateTime d) {
        this.datePose = d;
    }

    public List<HistoriquePixel> getHistorique() {
        return historique;
    }
}
