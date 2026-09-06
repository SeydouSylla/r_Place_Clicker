package com.rplace.controleur;

import com.rplace.dto.BonusDto;
import com.rplace.modele.JoueurBonus;
import com.rplace.repository.JoueurBonusRepository;
import com.rplace.service.BonusService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

// Ce controleur gere les requetes REST liees aux bonus Cookie Clicker
// Il expose deux routes : lister les bonus disponibles et acheter un bonus
// Source @RestController :
// https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-controller/ann-methods/responsebody.html
@RestController
@RequestMapping("/api/bonus")
public class ControleurBonus {

    private final BonusService bonusService;
    private final JoueurBonusRepository joueurBonusRepository;

    // Injection par constructeur
    public ControleurBonus(BonusService bonusService,
                           JoueurBonusRepository joueurBonusRepository) {
        this.bonusService = bonusService;
        this.joueurBonusRepository = joueurBonusRepository;
    }

    // GET /api/bonus
    // Retourne tous les bonus disponibles avec la quantite possedee
    // par le joueur connecte pour chaque bonus
    // Nous utilisons stream et map pour convertir chaque TypeBonus en BonusDto
    // Source Stream API :
    // https://docs.oracle.com/en/java/docs/api/java.base/java/util/stream/Stream.html
    @GetMapping
    public ResponseEntity<List<BonusDto>> listerBonus(
            @AuthenticationPrincipal UserDetails utilisateur) {
        Long joueurId = Long.parseLong(utilisateur.getUsername());
        List<BonusDto> dtos = bonusService.getTousLesBonus().stream()
                .map(typeBonus -> {
                    // Pour chaque bonus on recupere la quantite possedee par le joueur
                    // Si le joueur ne possede pas ce bonus on retourne 0
                    int quantite = joueurBonusRepository
                            .findByJoueurIdentifiantAndTypeBonusIdentifiant(
                                    joueurId, typeBonus.getIdentifiant())
                            .map(JoueurBonus::getQuantite).orElse(0);
                    return BonusDto.depuisTypeBonus(typeBonus, quantite);
                }).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    // POST /api/bonus/{id}/acheter
    // Achete un bonus pour le joueur connecte
    // @PathVariable recupere l'identifiant du bonus depuis l'URL
    // Source @PathVariable :
    // https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-controller/ann-methods/pathvariables.html
    @PostMapping("/{id}/acheter")
    public ResponseEntity<BonusDto> acheterBonus(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails utilisateur) {
        Long joueurId = Long.parseLong(utilisateur.getUsername());
        JoueurBonus joueurBonus = bonusService.acheterBonus(joueurId, id);
        // Nous retournons le bonus mis a jour avec la nouvelle quantite
        return ResponseEntity.ok(BonusDto.depuisTypeBonus(
                joueurBonus.getTypeBonus(), joueurBonus.getQuantite()));
    }
}