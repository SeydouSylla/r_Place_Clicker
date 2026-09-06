package com.rplace.modele;

import jakarta.persistence.*;

import java.time.LocalDateTime;

//Enregistre chaque placement de pixel.
//Permet les statistiques : total poses, pixel le plus ancien, records.

@Entity
@Table(name = "historique_pixel")
public class HistoriquePixel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long identifiant;

    @Column(name = "position_x", nullable = false)
    private int positionX;

    @Column(name = "position_y", nullable = false)
    private int positionY;

    @Column(name = "couleur", length = 7, nullable = false)
    private String couleur;

    // Joueur qui a pose ce pixel
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "joueur_id", nullable = false)
    private Joueur joueur;

    // Pixel concerne (permet de verifier s'il est encore en place)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pixel_id", nullable = false)
    private Pixel pixel;

    // Cout paye au moment du placement (pour l'historique economique)
    @Column(name = "cout_paye", nullable = false)
    private int coutPaye;

    @Column(name = "date_pose", nullable = false)
    private LocalDateTime datePose = LocalDateTime.now();

    //Constructeurs
    public HistoriquePixel() {
    }

    public HistoriquePixel(Joueur joueur, Pixel pixel, String couleur, int coutPaye) {
        this.joueur = joueur;
        this.pixel = pixel;
        this.positionX = pixel.getPositionX();
        this.positionY = pixel.getPositionY();
        this.couleur = couleur;
        this.coutPaye = coutPaye;
    }

    //Getters
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

    public Joueur getJoueur() {
        return joueur;
    }

    public Pixel getPixel() {
        return pixel;
    }

    public int getCoutPaye() {
        return coutPaye;
    }

    public LocalDateTime getDatePose() {
        return datePose;
    }
}
