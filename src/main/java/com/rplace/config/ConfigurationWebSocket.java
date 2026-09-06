package com.rplace.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.*;

// Nous avons utilise WebSocket avec STOMP pour que la grille se mette
// a jour en temps reel pour tous les joueurs connectes
// Nous avons utilise comme source : https://docs.spring.io/spring-framework/reference/web/websocket/stomp/enable.html
@Configuration
@EnableWebSocketMessageBroker
public class ConfigurationWebSocket implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registre) {
        // /topic sert a envoyer un pixel a tous les clients en meme temps
        // /app sert a recevoir les messages envoyes par les clients
        registre.enableSimpleBroker("/topic");
        registre.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registre) {
        // Nous avons ajoute SockJS comme solution de secours au cas ou
        // le navigateur du joueur ne supporte pas WebSocket
        // Nous avons utilise Source SockJS : https://docs.spring.io/spring-framework/reference/web/websocket/fallback.html
        registre.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }
}