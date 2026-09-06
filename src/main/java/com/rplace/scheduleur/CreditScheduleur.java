package com.rplace.scheduleur;

import com.rplace.service.BonusService;
import com.rplace.service.CreditService;
import com.rplace.repository.JoueurRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

// Ce scheduler regenere automatiquement les credits de tous les joueurs
// toutes les 10 secondes comme dans Cookie Clicker
// @EnableScheduling est active dans MonProjetApplication
// Source @Scheduled :
// https://docs.spring.io/spring-framework/reference/integration/scheduling.html
@Component
public class CreditScheduleur {

    private final JoueurRepository joueurRepository;
    private final CreditService creditService;
    private final BonusService bonusService;

    // Injection par constructeur
    public CreditScheduleur(JoueurRepository joueurRepository,
                            CreditService creditService,
                            BonusService bonusService) {
        this.joueurRepository = joueurRepository;
        this.creditService = creditService;
        this.bonusService = bonusService;
    }

    // S'execute automatiquement toutes les 10 secondes
    // Pour chaque joueur nous calculons sa production selon ses bonus
    // puis nous ajoutons les credits correspondants
    // fixedRate = 10000 signifie toutes les 10000 millisecondes
    @Scheduled(fixedRate = 10000)
    public void regenererCredits() {
        joueurRepository.findAll().forEach(joueur -> {
            // On calcule la production du joueur selon ses generateurs achetes
            double production = bonusService.calculerProduction(
                    joueur.getIdentifiant());
            // Nous ajoutons les credits au joueur
            creditService.regenerer(joueur.getIdentifiant(), production);
        });
    }
}