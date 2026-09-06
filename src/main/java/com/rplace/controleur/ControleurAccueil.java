package com.rplace.controleur;

import com.rplace.modele.Joueur;
import com.rplace.service.JoueurService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

// Controleur MVC pour les pages de navigation principales.
// Qui retourne les noms des templates Thymeleaf.
// Spring Boot les trouve automatiquement dans resources/templates/.
@Controller
public class ControleurAccueil {

    private final JoueurService joueurService;

    public ControleurAccueil(JoueurService joueurService) {
        this.joueurService = joueurService;
    }

    // GET / et /accueil - Page d'accueil publique.
    @GetMapping({"/", "/accueil"})
    public String accueil() {

        return "accueil/accueil"; // => templates/accueil/accueil.html
    }

    // GET /jeu - Page principale du jeu.
    // Le cahier des charges precise qu'un visiteur non connecte doit pouvoir
    // voir la grille en temps reel. Du coup on a rendu @AuthenticationPrincipal
    // optionnel : s'il est null c'est un visiteur, sinon c'est un joueur connecte
    // Le template Thymeleaf gere deja les deux cas avec sec:authorize

    @GetMapping("/jeu")
    public String jeu(Model modele,
                      @AuthenticationPrincipal UserDetails utilisateur) {
        // Si l'utilisateur est connecte on injecte ses donnees dans le modele
        // sinon on ne met rien et la vue affichera juste la grille en lecture seule
        if (utilisateur != null) {
            Long joueurId = Long.parseLong(utilisateur.getUsername());
            Joueur joueur = joueurService.trouverParId(joueurId);
            modele.addAttribute("joueur", joueur);
        }

        return "jeu/jeu"; // => templates/jeu/jeu.html
    }


    // GET /regles - Page des regles, accessible a tous.

    @GetMapping("/regles")
    public String regles() {

        return "regles/regles"; // -> templates/regles/regles.html
    }
}