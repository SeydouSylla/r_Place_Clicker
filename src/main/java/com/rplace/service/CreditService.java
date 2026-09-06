package com.rplace.service;

import com.rplace.modele.Joueur;
import com.rplace.repository.JoueurRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

// Ce service gere uniquement les credits des joueurs
// Nous avons separe la logique des credits de celle des pixels pour garder
// chaque classe avec une seule responsabilite (principe SRP)
// Source @Transactional :
// https://docs.spring.io/spring-framework/reference/data-access/transaction.html
@Service
@Transactional
public class CreditService {

    private final JoueurRepository joueurRepository;

    // Injection par constructeur
    public CreditService(JoueurRepository joueurRepository) {
        this.joueurRepository = joueurRepository;
    }

    // Minage manuel : le joueur clique sur le bouton et gagne 1 credit
    // Nous mettons aussi a jour la date de derniere activite du joueur
    public long miner(Long joueurId) {
        Joueur joueur = joueurRepository.findById(joueurId)
                .orElseThrow(() -> new RuntimeException("Joueur introuvable"));
        joueur.setCredits(joueur.getCredits() + 1);
        joueur.setDerniereActivite(LocalDateTime.now());
        joueurRepository.save(joueur);
        return joueur.getCredits();
    }

    // Regeneration automatique appelee par le scheduler toutes les 10 secondes
    // La production depend des bonus achetes par le joueur
    // Math.max garantit un minimum de 1 credit meme sans aucun bonus
    // Source Math.max : https://docs.oracle.com/en/java/docs/api/java.base/java/lang/Math.html
    public void regenerer(Long joueurId, double production) {
        Joueur joueur = joueurRepository.findById(joueurId)
                .orElseThrow(() -> new RuntimeException("Joueur introuvable"));
        // On arrondit la production et on garantit au moins 1 credit
        long creditsAjoutes = Math.max(1, Math.round(production));
        joueur.setCredits(joueur.getCredits() + creditsAjoutes);
        joueurRepository.save(joueur);
    }

    // Retourne le solde actuel du joueur sans modifier la base de donnees
    // readOnly = true pour optimiser la requete en lecture seule
    @Transactional(readOnly = true)
    public long getCredits(Long joueurId) {
        return joueurRepository.findById(joueurId)
                .map(Joueur::getCredits).orElse(0L);
    }
}