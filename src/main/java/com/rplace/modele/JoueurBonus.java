package com.rplace.modele;

import jakarta.persistence.*;

import java.time.LocalDateTime;

//Stocke la quantite possedee et la date du premier achat.
@Entity
@Table(name = "joueur_bonus",
        uniqueConstraints = @UniqueConstraint(columnNames = {"joueur_id", "type_bonus_id"}))
public class JoueurBonus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long identifiant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "joueur_id", nullable = false)
    private Joueur joueur;

    // EAGER car on a toujours besoin des infos du bonus quand on charge JoueurBonus
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "type_bonus_id", nullable = false)
    private TypeBonus typeBonus;

    @Column(name = "quantite", nullable = false)
    private int quantite = 1;

    @Column(name = "date_achat", nullable = false)
    private LocalDateTime dateAchat = LocalDateTime.now();

    //Methodes metier

    //Constructeurs
    public JoueurBonus() {
    }

    public JoueurBonus(Joueur joueur, TypeBonus typeBonus) {
        this.joueur = joueur;
        this.typeBonus = typeBonus;
    }

    // Production totale en credits/seconde pour ce bonus.
    // Production = quantite x multiplicateur du type.
    public double getProductionTotale() {
        return quantite * typeBonus.getMultiplicateur();
    }

    //Getters et Setters
    public Long getIdentifiant() {
        return identifiant;
    }

    public Joueur getJoueur() {
        return joueur;
    }

    public TypeBonus getTypeBonus() {
        return typeBonus;
    }

    public int getQuantite() {
        return quantite;
    }

    public void setQuantite(int q) {
        this.quantite = q;
    }

    public LocalDateTime getDateAchat() {
        return dateAchat;
    }
}
