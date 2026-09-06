package com.rplace.service;

import com.rplace.exception.CreditInsuffisantException;
import com.rplace.modele.*;
import com.rplace.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

// Ce service gere l'achat des bonus et le calcul de la production de credits
// C'est la partie Cookie Clicker du projet - les generateurs automatiques
// Source @Transactional :
// https://docs.spring.io/spring-framework/reference/data-access/transaction.html
@Service
@Transactional
public class BonusService {

    private final JoueurRepository joueurRepository;
    private final JoueurBonusRepository joueurBonusRepository;
    private final TypeBonusRepository typeBonusRepository;

    // Injection par constructeur
    public BonusService(JoueurRepository joueurRepository,
                        JoueurBonusRepository joueurBonusRepository,
                        TypeBonusRepository typeBonusRepository) {
        this.joueurRepository = joueurRepository;
        this.joueurBonusRepository = joueurBonusRepository;
        this.typeBonusRepository = typeBonusRepository;
    }

    // Achete un bonus pour le joueur
    // Si le joueur possede deja ce bonus on incremente juste la quantite
    // Le prix augmente de 15% a chaque achat supplementaire
    // Tout se fait dans une seule transaction - si le debit echoue
    // l'achat est annule automatiquement
    public JoueurBonus acheterBonus(Long joueurId, Long typeBonusId) {
        Joueur joueur = joueurRepository.findById(joueurId)
                .orElseThrow(() -> new RuntimeException("Joueur introuvable"));
        TypeBonus typeBonus = typeBonusRepository.findById(typeBonusId)
                .orElseThrow(() -> new RuntimeException("Bonus introuvable"));

        // On recupere la quantite deja possedee pour calculer le bon prix
        int quantiteActuelle = joueurBonusRepository
                .findByJoueurIdentifiantAndTypeBonusIdentifiant(joueurId, typeBonusId)
                .map(JoueurBonus::getQuantite).orElse(0);

        int prix = typeBonus.getPrixPourQuantite(quantiteActuelle);

        // On verifie que le joueur a assez de credits
        if (joueur.getCredits() < prix) {
            throw new CreditInsuffisantException(
                    "Credits insuffisants pour acheter ce bonus");
        }

        // On debite les credits du joueur
        joueur.setCredits(joueur.getCredits() - prix);
        joueurRepository.save(joueur);

        // Si le joueur possede deja ce bonus on incremente la quantite
        // sinon on cree une nouvelle entree JoueurBonus
        JoueurBonus joueurBonus = joueurBonusRepository
                .findByJoueurIdentifiantAndTypeBonusIdentifiant(joueurId, typeBonusId)
                .orElse(new JoueurBonus(joueur, typeBonus));
        joueurBonus.setQuantite(joueurBonus.getQuantite() + 1);

        return joueurBonusRepository.save(joueurBonus);
    }

    // Calcule la production totale de credits par seconde du joueur
    // Nous additionnons la production de chaque bonus possede
    // Source Stream mapToDouble :
    // https://docs.oracle.com/en/java/docs/api/java.base/java/util/stream/Stream.html
    @Transactional(readOnly = true)
    public double calculerProduction(Long joueurId) {
        return joueurBonusRepository.findByJoueurIdentifiant(joueurId)
                .stream().mapToDouble(JoueurBonus::getProductionTotale).sum();
    }

    // Retourne tous les types de bonus disponibles dans le catalogue
    @Transactional(readOnly = true)
    public List<TypeBonus> getTousLesBonus() {
        return typeBonusRepository.findAll();
    }
}