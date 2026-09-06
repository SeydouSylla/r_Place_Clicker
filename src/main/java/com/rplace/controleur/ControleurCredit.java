package com.rplace.controleur;

import com.rplace.service.CreditService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

// Ce controleur gere les requetes REST liees aux credits
// Il expose deux routes : consulter le solde et miner un credit
// Source @RestController :
// https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-controller/ann-methods/responsebody.html
@RestController
@RequestMapping("/api/credits")
public class ControleurCredit {

    private final CreditService creditService;

    // Injection par constructeur
    public ControleurCredit(CreditService creditService) {
        this.creditService = creditService;
    }

    // GET /api/credits
    // Retourne le solde actuel du joueur connecte
    // @AuthenticationPrincipal recupere le joueur depuis Spring Security
    // Source @AuthenticationPrincipal :
    // https://docs.spring.io/spring-security/reference/servlet/integrations/mvc.html#mvc-authentication-principal
    @GetMapping
    public ResponseEntity<Map<String, Long>> getCredits(
            @AuthenticationPrincipal UserDetails utilisateur) {
        Long joueurId = Long.parseLong(utilisateur.getUsername());
        return ResponseEntity.ok(
                Map.of("credits", creditService.getCredits(joueurId)));
    }

    // POST /api/credits/miner
    // Ajoute 1 credit au joueur quand il clique sur le bouton Miner
    // Map.of cree une reponse JSON simple { "credits": 51 }
    // Source Map.of :
    // https://docs.oracle.com/en/java/docs/api/java.base/java/util/Map.html
    @PostMapping("/miner")
    public ResponseEntity<Map<String, Long>> miner(
            @AuthenticationPrincipal UserDetails utilisateur) {
        Long joueurId = Long.parseLong(utilisateur.getUsername());
        return ResponseEntity.ok(
                Map.of("credits", creditService.miner(joueurId)));
    }
}