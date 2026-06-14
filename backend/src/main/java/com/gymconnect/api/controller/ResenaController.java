package com.gymconnect.api.controller;

import com.gymconnect.api.model.Entrenador;
import com.gymconnect.api.model.Relacion;
import com.gymconnect.api.model.Resena;
import com.gymconnect.api.model.Usuario;
import com.gymconnect.api.repository.EntrenadorRepository;
import com.gymconnect.api.repository.RelacionRepository;
import com.gymconnect.api.repository.ResenaRepository;
import com.gymconnect.api.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/resenas")
@RequiredArgsConstructor
public class ResenaController {

    private final ResenaRepository resenaRepo;
    private final EntrenadorRepository entrenadorRepo;
    private final UsuarioRepository usuarioRepo;
    private final RelacionRepository relacionRepo;

    // Público: listar reseñas de un entrenador
    @GetMapping("/entrenador/{entrenadorId}")
    public ResponseEntity<List<Resena>> listar(@PathVariable Long entrenadorId) {
        return ResponseEntity.ok(resenaRepo.findByEntrenadorIdOrderByCreadaEnDesc(entrenadorId));
    }

    // Cliente: crear o actualizar su reseña (solo si tiene relación activa)
    @PostMapping("/entrenador/{entrenadorId}")
    public ResponseEntity<?> crear(@AuthenticationPrincipal UserDetails ud,
                                    @PathVariable Long entrenadorId,
                                    @RequestBody Map<String, Object> body) {
        Usuario cliente = usuarioRepo.findByEmail(ud.getUsername())
                .orElseThrow(() -> new RuntimeException("No encontrado"));

        Entrenador entrenador = entrenadorRepo.findById(entrenadorId)
                .orElseThrow(() -> new RuntimeException("Entrenador no encontrado"));

        // Solo clientes con relación activa pueden reseñar
        boolean esClienteActivo = relacionRepo.existsByClienteIdAndEntrenadorIdAndEstado(
                cliente.getId(), entrenadorId, Relacion.Estado.ACTIVA);
        if (!esClienteActivo)
            return ResponseEntity.status(403).body(Map.of("error", "Solo tus clientes activos pueden dejar una reseña"));

        int estrellas = ((Number) body.get("estrellas")).intValue();
        if (estrellas < 1 || estrellas > 5)
            return ResponseEntity.badRequest().body(Map.of("error", "Las estrellas deben ser entre 1 y 5"));

        // Una reseña por cliente por entrenador (upsert)
        Resena r = resenaRepo.findByClienteIdAndEntrenadorId(cliente.getId(), entrenadorId)
                .orElse(new Resena());
        r.setCliente(cliente);
        r.setEntrenador(entrenador);
        r.setEstrellas(estrellas);
        r.setComentario((String) body.get("comentario"));

        Resena saved = resenaRepo.save(r);

        // Recalcular rating del entrenador
        List<Resena> todas = resenaRepo.findByEntrenadorIdOrderByCreadaEnDesc(entrenadorId);
        double avg = todas.stream().mapToInt(Resena::getEstrellas).average().orElse(5.0);
        entrenador.setRating(Math.round(avg * 10.0) / 10.0);
        entrenador.setTotalResenas(todas.size());
        entrenadorRepo.save(entrenador);

        return ResponseEntity.ok(saved);
    }

    // Cliente: ver si ya ha dejado reseña a este entrenador
    @GetMapping("/entrenador/{entrenadorId}/mia")
    public ResponseEntity<?> miResena(@AuthenticationPrincipal UserDetails ud,
                                       @PathVariable Long entrenadorId) {
        Usuario cliente = usuarioRepo.findByEmail(ud.getUsername())
                .orElseThrow(() -> new RuntimeException("No encontrado"));
        return resenaRepo.findByClienteIdAndEntrenadorId(cliente.getId(), entrenadorId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }
}
