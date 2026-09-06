package com.rplace.controleur;

import com.rplace.dto.RequeteMotDePasse;
import com.rplace.dto.RequeteProfil;
import com.rplace.modele.Joueur;
import com.rplace.service.JoueurService;
import com.rplace.service.StatService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

// Controleur MVC qui gere la page profil du joueur connecte
// On utilise @Controller (et pas @RestController) car on retourne des vues Thymeleaf
// et non du JSON
// Toutes les routes commencent par /profil grace au @RequestMapping de la classe
// Source Spring MVC :
// https://docs.spring.io/spring-framework/reference/web/webmvc.html
@Controller
@RequestMapping("/profil")
public class ControleurProfil {

    private final JoueurService joueurService;
    private final StatService statService;

    public ControleurProfil(JoueurService joueurService,
                            StatService statService) {
        this.joueurService = joueurService;
        this.statService = statService;
    }

    // Methode utilitaire privee pour remplir le modele avec toutes les donnees
    // necessaires a l'affichage de la page profil. On en a sortit le code car
    // il est appele depuis plusieurs endpoints (GET et les POST en cas d'erreur)
    // Ca evite la duplication
    private void remplirModele(Model modele, Long joueurId) {
        Joueur joueur = joueurService.trouverParId(joueurId);
        modele.addAttribute("joueur", joueur);
        if (!modele.containsAttribute("requete")) {
            modele.addAttribute("requete", new RequeteProfil());
        }
        if (!modele.containsAttribute("requeteMdp")) {
            modele.addAttribute("requeteMdp", new RequeteMotDePasse());
        }
        modele.addAttribute("nbPixelsTotaux",
                statService.getNbPixelsTotaux(joueurId));
        modele.addAttribute("pixelLePlusAncien",
                statService.getPixelLePlusAncienEnPlace(joueurId));
        modele.addAttribute("recordAge",
                StatService.formaterDuree(statService.getRecordAgePixel(joueurId)));
    }

    // Affiche la page de profil du joueur connecte (GET /profil)
    // @AuthenticationPrincipal permet de recuperer directement l'utilisateur connecte
    // sans avoir a aller chercher le SecurityContext manuellement
    // On stocke l'id du joueur dans le username de UserDetails
    @GetMapping
    public String afficherProfil(Model modele,
                                 @AuthenticationPrincipal UserDetails utilisateur) {
        Long joueurId = Long.parseLong(utilisateur.getUsername());
        remplirModele(modele, joueurId);
        return "profil/profil";
    }

    // Traite la modification du profil age et pays (POST /profil)
    // @Valid declenche la validation du DTO selon les contraintes definies
    // BindingResult DOIT etre place juste apres @Valid sinon Spring leve une exception
    @PostMapping
    public String modifierProfil(
            @Valid @ModelAttribute("requete") RequeteProfil requete,
            BindingResult resultat, Model modele,
            @AuthenticationPrincipal UserDetails utilisateur) {
        Long joueurId = Long.parseLong(utilisateur.getUsername());
        if (resultat.hasErrors()) {
            remplirModele(modele, joueurId);
            return "profil/profil";
        }
        joueurService.modifierProfil(joueurId, requete.getAge(), requete.getPays());
        // Pattern PRG : on redirige apres POST pour eviter qu'un F5 reposte le form
        return "redirect:/profil?modificationReussie";
    }

    // Traite le changement de mot de passe (POST /profil/mot-de-passe)
    // tous les champs sauf le pseudo peuvent etre modifies,
    // donc le mot de passe doit etre modifiable
    // On verifie en plus que les deux champs (nouveau + confirmation) correspondent
    @PostMapping("/mot-de-passe")
    public String changerMotDePasse(
            @Valid @ModelAttribute("requeteMdp") RequeteMotDePasse requeteMdp,
            BindingResult resultat, Model modele,
            @AuthenticationPrincipal UserDetails utilisateur) {

        Long joueurId = Long.parseLong(utilisateur.getUsername());

        // Verification supplementaire : les deux mots de passe doivent correspondre
        // C'est pas une validation Jakarta classique donc on le fait a la main
        // Source rejectValue :
        // https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/validation/Errors.html
        if (!resultat.hasErrors() && !requeteMdp.getNouveauMotDePasse()
                .equals(requeteMdp.getConfirmation())) {
            resultat.rejectValue("confirmation", "differents",
                    "Les deux mots de passe ne correspondent pas");
        }

        if (resultat.hasErrors()) {
            remplirModele(modele, joueurId);
            return "profil/profil";
        }

        joueurService.changerMotDePasse(joueurId, requeteMdp.getNouveauMotDePasse());
        return "redirect:/profil?motDePasseChange";
    }
}