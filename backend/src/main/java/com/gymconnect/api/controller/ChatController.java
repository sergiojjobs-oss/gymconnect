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
    private final com.gymconnect.api.config.PresenciaService presenciaService;

    // WebSocket: indicador de escritura
    @MessageMapping("/chat.typing")
    public void typing(@Payload java.util.Map<String, Object> payload,
                       java.security.Principal principal) {
        Usuario yo = usuarioRepo.findByEmail(principal.getName()).orElseThrow();
        Long destId = Long.parseLong(payload.get("destinatarioId").toString());
        Usuario dest = usuarioRepo.findById(destId).orElseThrow();
        broker.convertAndSendToUser(dest.getEmail(), "/queue/typing",
            java.util.Map.of("remitenteId", yo.getId(), "escribiendo", payload.get("escribiendo")));
    }

    // WebSocket: cliente envía a /app/chat.enviar
    @MessageMapping("/chat.enviar")
    public void enviarMensaje(@Payload MensajeInput input,
                               java.security.Principal principal) {
        Usuario remitente = usuarioRepo.findByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Mensaje guardado = chatService.guardar(
                remitente.getId(), input.getDestinatarioId(), input.getContenido(), input.getReplyToId());

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
            Map<Long, Map<String, Object>> vistos = new LinkedHashMap<>();

            // 1. Relaciones activas primero (clientes que han pagado)
            var relaciones = relacionRepo.findByEntrenadorUsuarioIdAndEstado(yo.getId(), Relacion.Estado.ACTIVA);
            for (Relacion r : relaciones) {
                Long cid = r.getCliente().getId();
                Map<String, Object> c = new HashMap<>();
                c.put("id", cid);
                c.put("nombre", r.getCliente().getNombre() + " " + r.getCliente().getApellido());
                c.put("esPagador", true);
                vistos.put(cid, c);
            }

            // 2. Resto de usuarios con mensajes (sin relación activa)
            for (Long interlocutorId : mensajeRepo.findInterlocutorIds(yo.getId())) {
                if (!vistos.containsKey(interlocutorId)) {
                    usuarioRepo.findById(interlocutorId).ifPresent(u -> {
                        Map<String, Object> c = new HashMap<>();
                        c.put("id", u.getId());
                        c.put("nombre", u.getNombre() + " " + u.getApellido());
                        c.put("esPagador", false);
                        vistos.put(u.getId(), c);
                    });
                }
            }

            resultado.addAll(vistos.values());
        } else {
            // Cliente: entrenadores activos primero
            Map<Long, Map<String, Object>> vistos = new LinkedHashMap<>();
            var relaciones = relacionRepo.findByClienteIdAndEstado(yo.getId(), Relacion.Estado.ACTIVA);
            for (Relacion r : relaciones) {
                Long eid = r.getEntrenador().getUsuario().getId();
                Map<String, Object> c = new HashMap<>();
                c.put("id", eid);
                c.put("nombre", r.getEntrenador().getUsuario().getNombre() + " " + r.getEntrenador().getUsuario().getApellido());
                c.put("esPagador", true);
                vistos.put(eid, c);
            }
            // Resto de usuarios con mensajes previos
            for (Long interlocutorId : mensajeRepo.findInterlocutorIds(yo.getId())) {
                if (!vistos.containsKey(interlocutorId)) {
                    usuarioRepo.findById(interlocutorId).ifPresent(u -> {
                        Map<String, Object> c = new HashMap<>();
                        c.put("id", u.getId());
                        c.put("nombre", u.getNombre() + " " + u.getApellido());
                        c.put("esPagador", false);
                        vistos.put(u.getId(), c);
                    });
                }
            }
            resultado.addAll(vistos.values());
        }

        return ResponseEntity.ok(resultado);
    }

    // REST: eliminar mensaje individual (solo el remitente puede borrarlo)
    @DeleteMapping("/api/chat/mensaje/{mensajeId}")
    public ResponseEntity<Void> eliminarMensaje(
            @PathVariable Long mensajeId,
            @AuthenticationPrincipal UserDetails ud) {

        Usuario yo = usuarioRepo.findByEmail(ud.getUsername())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        var opt = mensajeRepo.findById(mensajeId);
        if (opt.isEmpty() || !opt.get().getRemitenteId().equals(yo.getId()))
            return ResponseEntity.notFound().build();
        Mensaje m = opt.get();
        m.setEliminado(true);
        m.setContenido("");
        mensajeRepo.save(m);
        return ResponseEntity.ok().build();
    }

    // REST: eliminar conversación (todos los mensajes entre dos usuarios)
    @DeleteMapping("/api/chat/{otroId}/conversacion")
    public ResponseEntity<Void> eliminarConversacion(
            @PathVariable Long otroId,
            @AuthenticationPrincipal UserDetails ud) {

        Usuario yo = usuarioRepo.findByEmail(ud.getUsername())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Solo borra los mensajes que YO he enviado (no los del otro)
        mensajeRepo.deleteByRemitenteIdAndDestinatarioId(yo.getId(), otroId);
        return ResponseEntity.ok().build();
    }

    // REST: consultar si un usuario está online (solo si existe relación o conversación)
    @GetMapping("/api/chat/presencia/{userId}")
    public ResponseEntity<?> presencia(@PathVariable Long userId,
                                        @AuthenticationPrincipal UserDetails ud) {
        if (ud == null) return ResponseEntity.status(401).build();
        return usuarioRepo.findById(userId)
            .map(u -> (ResponseEntity<?>) ResponseEntity.ok(Map.of("online", presenciaService.estaConectado(u.getEmail()))))
            .orElse(ResponseEntity.notFound().build());
    }

    // REST: mensajes no leídos
    @GetMapping("/api/chat/no-leidos")
    public ResponseEntity<Map<String, Long>> noLeidos(
            @AuthenticationPrincipal UserDetails ud) {

        Usuario yo = usuarioRepo.findByEmail(ud.getUsername())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        return ResponseEntity.ok(Map.of("total", chatService.noLeidos(yo.getId())));
    }

    // REST: marcar mensajes de una conversación como leídos
    @PostMapping("/api/chat/{otroId}/leer")
    public ResponseEntity<Void> marcarLeidos(
            @PathVariable Long otroId,
            @AuthenticationPrincipal UserDetails ud) {

        Usuario yo = usuarioRepo.findByEmail(ud.getUsername())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        chatService.marcarLeidos(yo.getId(), otroId);
        return ResponseEntity.ok().build();
    }
}
