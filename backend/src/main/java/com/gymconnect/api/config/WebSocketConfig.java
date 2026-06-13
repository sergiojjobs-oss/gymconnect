package com.gymconnect.api.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // El broker enruta mensajes hacia /topic y /queue
        registry.enableSimpleBroker("/topic", "/queue");
        // Los mensajes enviados al servidor tienen prefijo /app
        registry.setApplicationDestinationPrefixes("/app");
        // Para mensajes de usuario específico
        registry.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Endpoint de conexión WebSocket, con fallback SockJS para navegadores viejos
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns(
                    "http://localhost:3000",
                    "http://localhost:5500",
                    "http://127.0.0.1:5500",
                    "https://splendid-daffodil-79285f.netlify.app",
                    "https://gymconnect-six.vercel.app"
                )
                .withSockJS();
    }
}
