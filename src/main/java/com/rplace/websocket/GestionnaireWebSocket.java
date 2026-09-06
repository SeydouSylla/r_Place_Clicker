package com.rplace.websocket;

import com.rplace.dto.PixelDto;
import com.rplace.dto.RequetePixel;
import com.rplace.modele.Pixel;
import com.rplace.service.PixelService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;
import java.security.Principal;

// Ce gestionnaire recoit les pixels envoyes par les joueurs via WebSocket
// et les envoie a tous les autres joueurs connectes en temps reel
// Source @MessageMapping et @SendTo :
// https://docs.spring.io/spring-framework/reference/web/websocket/stomp/handle-annotations.html
@Controller
public class GestionnaireWebSocket {

    private final PixelService pixelService;

    // Injection par constructeur
    public GestionnaireWebSocket(PixelService pixelService) {
        this.pixelService = pixelService;
    }

    // Recoit un pixel depuis un joueur et le broadcast a tous les abonnes
    // @MessageMapping : ecoute les messages envoyes sur /app/pixel
    // @SendTo : renvoie le resultat a tous les abonnes de /topic/grille
    // On utilise SimpMessageHeaderAccessor pour recuperer le joueur connecte
    // car @AuthenticationPrincipal ne fonctionne pas dans le contexte WebSocket
    // Source SimpMessageHeaderAccessor :
    // https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/messaging/simp/SimpMessageHeaderAccessor.html
    @MessageMapping("/pixel")
    @SendTo("/topic/grille")
    public PixelDto gererNouveauPixel(
            RequetePixel requete,
            SimpMessageHeaderAccessor headerAccessor) {

        // On recupere le joueur connecte depuis le contexte WebSocket
        Principal principal = headerAccessor.getUser();

        System.out.println("=== WebSocket recu ===");
        System.out.println("Principal : " + principal);
        System.out.println("Requete x=" + requete.getX()
                + " y=" + requete.getY()
                + " couleur=" + requete.getCouleur());

        // Si le joueur n'est pas connecte on lance une exception
        if (principal == null) {
            System.out.println("ERREUR : principal null");
            throw new RuntimeException("Utilisateur non authentifie");
        }

        try {
            // Le username correspond a l'identifiant du joueur dans notre cas
            Long joueurId = Long.parseLong(principal.getName());
            System.out.println("JoueurId : " + joueurId);

            // On place le pixel en base et on retourne le DTO
            // qui sera broadcast a tous les clients abonnes a /topic/grille
            Pixel pixel = pixelService.placerPixel(
                    joueurId,
                    requete.getX(),
                    requete.getY(),
                    requete.getCouleur()
            );

            System.out.println("Pixel sauvegarde OK");
            return PixelDto.depuisPixel(pixel);

        } catch (Exception e) {
            System.out.println("ERREUR placerPixel : " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
}