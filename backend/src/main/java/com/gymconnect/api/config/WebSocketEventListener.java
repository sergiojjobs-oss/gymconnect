package com.gymconnect.api.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.security.Principal;

@Component
@RequiredArgsConstructor
public class WebSocketEventListener {

    private final PresenciaService presenciaService;

    @EventListener
    public void onConnect(SessionConnectedEvent event) {
        Principal p = event.getUser();
        if (p != null) presenciaService.conectar(p.getName());
    }

    @EventListener
    public void onDisconnect(SessionDisconnectEvent event) {
        StompHeaderAccessor sha = StompHeaderAccessor.wrap(event.getMessage());
        Principal p = sha.getUser();
        if (p != null) presenciaService.desconectar(p.getName());
    }
}
