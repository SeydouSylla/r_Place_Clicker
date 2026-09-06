package com.rplace.controleur;

import com.rplace.dto.PixelDto;
import com.rplace.dto.RequetePixel;
import com.rplace.modele.Pixel;
import com.rplace.service.CreditService;
import com.rplace.service.PixelService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.util.List;

// Ce controleur gere toutes les requetes REST liees aux pixels
// Il repond aux appels HTTP du JavaScript cote client et retourne du JSON
// On a utilise @RestController qui combine @Controller et @ResponseBody
// Nous avons consulte comme source : https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-controller/ann-methods/responsebody.html

@RestController
@RequestMapping("/api/pixels")
public class ControleurPixel {

    private final PixelService pixelService;
    private final CreditService creditService;

    // Injection par constructeur - recommande par Spring
    // Nous avons utilise comme source: https://docs.spring.io/spring-framework/reference/core/beans/dependencies/factory-collaborators.html
    public ControleurPixel(PixelService pixelService,
                           CreditService creditService) {
        this.pixelService = pixelService;
        this.creditService = creditService;
    }

    // GET /api/pixels
    // Retourne tous les pixels de la grille en JSON
    // Cette route est accessible a tous les visiteurs meme non connectes
    // pour qu'ils puissent voir la grille sans se connecter
    @GetMapping
    public ResponseEntity<List<PixelDto>> getGrille() {
        return ResponseEntity.ok(pixelService.getGrilleComplete());
    }

    // GET /api/pixels/prix?x=5&y=10
    // Retourne le prix actuel d'un pixel selon sa position
    // Le prix augmente de 5 credits a chaque recouvrement
    // On utilise @RequestParam pour recuperer les parametres x et y dans l'URL
    // Source : https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-controller/ann-methods/requestparam.html
    @GetMapping("/prix")
    public ResponseEntity<Integer> getPrix(@RequestParam int x,
                                           @RequestParam int y) {
        return ResponseEntity.ok(pixelService.calculerPrix(x, y));
    }

    // POST /api/pixels, Permet de poser un pixel via REST
    // On utilise aussi le WebSocket pour le temps reel mais cette route
    // sert de solution de secours si le WebSocket ne fonctionne pas
    // @AuthenticationPrincipal recupere le joueur connecte depuis Spring Security
    // @RequestBody convertit automatiquement le JSON recu en objet RequetePixel
    // Nous avons consulte comme source (@RequestBody et @AuthenticationPrincipal): https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-controller/ann-methods/requestbody.html et https://docs.spring.io/spring-security/reference/servlet/integrations/mvc.html#mvc-authentication-principal
    @PostMapping
    public ResponseEntity<PixelDto> placerPixel(
            @RequestBody RequetePixel requete,
            @AuthenticationPrincipal UserDetails utilisateur) {
        // On recupere l'identifiant du joueur depuis Spring Security
        // Le username correspond a l'identifiant du joueur dans notre cas
        Long joueurId = Long.parseLong(utilisateur.getUsername());
        Pixel pixel = pixelService.placerPixel(
                joueurId, requete.getX(), requete.getY(), requete.getCouleur());
        return ResponseEntity.ok(PixelDto.depuisPixel(pixel));
    }
}