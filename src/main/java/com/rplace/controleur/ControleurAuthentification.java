package com.rplace.controleur;

import com.rplace.dto.RequeteInscription;
import com.rplace.service.JoueurService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

// Controleur MVC pour les pages d'authentification.
// Gere l'inscription (la connexion est geree directement par Spring Security).

@Controller
public class ControleurAuthentification {

    private final JoueurService joueurService;

    public ControleurAuthentification(JoueurService joueurService) {
        this.joueurService = joueurService;
    }

    // GET /connexion - Affiche le formulaire de connexion Thymeleaf.
    @GetMapping("/connexion")
    public String pageConnexion() {
        return "authentification/connexion";
    }

    // GET /inscription - Affiche le formulaire d'inscription. */
    @GetMapping("/inscription")
    public String pageInscription(Model modele) {
        // Injecter un objet vide pour le binding du formulaire Thymeleaf
        modele.addAttribute("requete", new RequeteInscription());
        return "authentification/inscription";
    }

    // POST /inscription - Traite le formulaire.
    // @Valid declenche la validation Jakarta Bean Validation.

    @PostMapping("/inscription")
    public String traiterInscription(
            @Valid @ModelAttribute("requete") RequeteInscription requete,
            BindingResult resultat,
            Model modele) {

        // S'il y a des erreurs, reafficher le formulaire avec les messages
        if (resultat.hasErrors()) {
            return "authentification/inscription";
        }

        try {
            joueurService.inscrire(
                    requete.getPseudo(), requete.getMotDePasse(),
                    requete.getAge(), requete.getPays());
            // Redirection apres inscription reussie
            return "redirect:/connexion?inscriptionReussie";
        } catch (IllegalArgumentException e) {
            modele.addAttribute("erreurGlobale", e.getMessage());
            return "authentification/inscription";
        }
    }
}
