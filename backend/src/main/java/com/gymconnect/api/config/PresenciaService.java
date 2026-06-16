package com.gymconnect.api.config;

import com.gymconnect.api.model.Usuario;
import com.gymconnect.api.repository.MensajeRepository;
import com.gymconnect.api.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class PresenciaService {

    private final Set<String> conectados = ConcurrentHashMap.newKeySet();
    private final SimpMessagingTemplate broker;
    private final UsuarioRepository usuarioRepo;
    private final MensajeRepository mensajeRepo;

    public void conectar(String email) {
        conectados.add(email);
        notificarContactos(email, true);
    }

    public void desconectar(String email) {
        conectados.remove(email);
        notificarContactos(email, false);
    }

    public boolean estaConectado(String email) {
        return conectados.contains(email);
    }

    private void notificarContactos(String email, boolean online) {
        usuarioRepo.findByEmail(email).ifPresent(yo -> {
            // Notificar a todos con quienes ha intercambiado mensajes
            mensajeRepo.findInterlocutorIds(yo.getId()).forEach(contactoId ->
                usuarioRepo.findById(contactoId).ifPresent(contacto ->
                    broker.convertAndSendToUser(contacto.getEmail(), "/queue/presencia",
                        Map.of("usuarioId", yo.getId(), "online", online))
                )
            );
        });
    }
}
