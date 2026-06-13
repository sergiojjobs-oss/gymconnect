package com.gymconnect.api.controller;

import com.gymconnect.api.dto.MensajeDto;
import com.gymconnect.api.dto.MensajeInput;
import com.gymconnect.api.model.Mensaje;
import com.gymconnect.api.model.Relacion;
import com.gymconnect.api.model.Usuario;
import com.gymconnect.api.repository.MensajeRepository;
import com.gymconnect.api.repository.RelacionRepository;
import com.gymconnect.api.repository.UsuarioRepository;
import com.gymconnect.api.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;
    private final UsuarioRepository usuarioRepo;
    private final RelacionRepository relacionRepo;
    private final MensajeRepository mensajeRepo;
    private final SimpMessagingTemplate broker;

    // WebSocket: cliente envía a /app/chat.enviar
    @MessageMapping("/chat.enviar")
    public void enviarMensaje(@Payload MensajeInput input,
                               java.security.Principal principal) {
        Usuario remitente = usuarioRepo.findByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Mensaje guardado = chatService.guardar(
                remitente.getId(), input.getDestinatarioId(), input.getContenido());

        MensajeDto dto = chatService.toDto(guardado);

        Usuario destinatario = usuarioRepo.findById(input.getDestinatarioId())
                .orElseThrow(() -> new RuntimeException("Destinatario no encontrado"));

        // Enviar al destinatario usando su email (Principal.name en WebSocket)
        broker.convertAndSendToUser(
                destinatario.getEmail(),
                "/queue/mensajes",
                dto
        );
        // Confirmar al remitente también
        broker.convertAndSendToUser(
                remitente.getEmail(),
                "/queue/mensajes",
                dto
        );
    }

    // REST: historial de conversación
    @GetMapping("/api/chat/{otroId}")
    public ResponseEntity<List<MensajeDto>> historial(
            @PathVariable Long otroId,
            @AuthenticationPrincipal UserDetails ud) {

        Usuario yo = usuarioRepo.findByEmail(ud.getUsername())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        return ResponseEntity.ok(chatService.getConversacion(yo.getId(), otroId));
    }

    // REST: lista de contactos del usuario (relaciones activas)
    @GetMapping("/api/chat/contactos")
    public ResponseEntity<List<Map<String, Object>>> contactos(
            @AuthenticationPrincipal UserDetails ud) {

        Usuario yo = usuarioRepo.findByEmail(ud.getUsername())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        List<Map<String, Object>> resultado = new ArrayList<>();

        if (yo.getRol() == Usuario.Rol.ENTRENADOR) {
            // Entrenador: combinar relaciones activas + usuarios con mensajes
            Map<Long, Map<String, Object>> vistos = new LinkedHashMap<>();

            // 1. Relaciones activas (clientes que han pagado)
            var relaciones = relacionRepo.findByEntrenadorUsuarioIdAndEstado(yo.getId(), Relacion.Estado.ACTIVA);
            for (Relacion r : relaciones) {
                Long cid = r.getCliente().getId();
                Map<String, Object> c = new HashMap<>();
                c.put("id", cid);
                c.put("nombre", r.getCliente().getNombre() + " " + r.getCliente().getApellido());
                vistos.put(cid, c);
            }

            // 2. Cualquier usuario que haya enviado/recibido mensajes
            for (Long interlocutorId : mensajeRepo.findInterlocutorIds(yo.getId())) {
                if (!vistos.containsKey(interlocutorId)) {
                    usuarioRepo.findById(interlocutorId).ifPresent(u -> {
                        Map<String, Object> c = new HashMap<>();
                        c.put("id", u.getId());
                        c.put("nombre", u.getNombre() + " " + u.getApellido());
                        vistos.put(u.getId(), c);
                    });
                }
            }

            resultado.addAll(vistos.values());
        } else {
            // Cliente: ver sus entrenadores activos
            var relaciones = relacionRepo.findByClienteIdAndEstado(yo.getId(), Relacion.Estado.ACTIVA);
            for (Relacion r : relaciones) {
                Map<String, Object> c = new HashMap<>();
                c.put("id", r.getEntrenador().getUsuario().getId());
                c.put("nombre", r.getEntrenador().getUsuario().getNombre() + " " + r.getEntrenador().getUsuario().getApellido());
                resultado.add(c);
            }
        }

        return ResponseEntity.ok(resultado);
    }

    // REST: mensajes no leídos
    @GetMapping("/api/chat/no-leidos")
    public ResponseEntity<Map<String, Long>> noLeidos(
            @AuthenticationPrincipal UserDetails ud) {

        Usuario yo = usuarioRepo.findByEmail(ud.getUsername())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        return ResponseEntity.ok(Map.of("total", chatService.noLeidos(yo.getId())));
    }
}
