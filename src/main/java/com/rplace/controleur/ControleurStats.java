package com.rplace.controleur;

import com.rplace.dto.JoueurDto;
import com.rplace.modele.Joueur;
import com.rplace.service.JoueurService;
import com.rplace.service.StatService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

// Controleur MVC qui gere les pages de statistiques et de classements
// On a deux pages differentes :
//   - /stats : la page generale avec les classements de tous les joueurs
//   - /stats/joueur/{id} : la page detaillee d'un joueur en particulier
// Comme pour le profil on retourne des vues Thymeleaf donc @Controller et pas @RestController
// Cette page est maintenant accessible aux visiteurs anonymes
// le classement doit etre visible par tous
// Source Spring MVC :
// https://docs.spring.io/spring-framework/reference/web/webmvc.html
@Controller
@RequestMapping("/stats")
public class ControleurStats {

    private final StatService statService;
    private final JoueurService joueurService;

    // Injection par constructeur des deux services
    // StatService pour les calculs de stats, JoueurService pour recuperer un joueur par son id
    public ControleurStats(StatService statService,
                           JoueurService joueurService) {
        this.statService = statService;
        this.joueurService = joueurService;
    }

    // Affiche la page generale des statistiques (GET /stats)
    // On envoie a la vue :
    //  - le classement par couverture (nb de pixels en place)
    //  - le classement par anciennete du plus vieux pixel encore en place
    //  - la liste de tous les joueurs inscrits
    @GetMapping
    public String afficherStats(Model modele) {
        modele.addAttribute("classementCouverture",
                statService.getClassementParCouverture());
        // Nouveau classement : ordre du plus vieux pixel par joueur
        modele.addAttribute("classementAncienetePixel",
                statService.getClassementParPlusVieuxPixel());
        modele.addAttribute("tousLesJoueurs",
                statService.getTousLesJoueurs());
        return "stats/stats";
    }

    // Affiche la page de stats detaillees d'un joueur precis (GET /stats/joueur/{id})
    // @PathVariable permet de recuperer la valeur dans l'URL et de la mettre dans le parametre id
    // Par exemple si on appelle /stats/joueur/42 alors id = 42
    @GetMapping("/joueur/{id}")
    public String afficherStatsJoueur(@PathVariable Long id, Model modele) {
        // On recupere l'entite Joueur puis on la convertit en DTO
        // pour ne pas exposer directement l'entite a la vue (separation des couches)
        Joueur joueur = joueurService.trouverParId(id);
        JoueurDto dto = JoueurDto.depuisJoueur(joueur);
        // Le nb de pixels totaux n'est pas dans l'entite Joueur, on doit le calculer
        // a part via le StatService et l'ajouter au DTO avant de l'envoyer a la vue
        dto.setNbPixelsTotalPoses(statService.getNbPixelsTotaux(id));
        modele.addAttribute("joueur", dto);
        // On envoie aussi la date d'inscription pour respecter le cahier des charges
        // qui demande d'afficher toutes les infos personnelles du joueur
        modele.addAttribute("dateInscription", joueur.getDateInscription());
        modele.addAttribute("pixelLePlusAncien",
                statService.getPixelLePlusAncienEnPlace(id));
        // Record d'age pour un pixel (peut concerner un pixel qui n'existe plus)
        // On l'envoie deja formate en chaine de caracteres pour simplifier la vue
        modele.addAttribute("recordAge",
                StatService.formaterDuree(statService.getRecordAgePixel(id)));
        return "stats/joueur";
    }
}