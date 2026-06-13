package com.gymconnect.api.controller;

import com.gymconnect.api.model.Usuario;
import com.gymconnect.api.repository.UsuarioRepository;
import com.gymconnect.api.service.SuscripcionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/suscripcion")
@RequiredArgsConstructor
public class SuscripcionController {

    private final SuscripcionService suscripcionService;
    private final UsuarioRepository usuarioRepo;

    @GetMapping("/estado")
    public ResponseEntity<?> miPlan(@AuthenticationPrincipal UserDetails ud) {
        Usuario u = usuarioRepo.findByEmail(ud.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        return ResponseEntity.ok(Map.of(
                "plan", u.getPlan(),
                "planExpira", u.getPlanExpira() != null ? u.getPlanExpira().toString() : null
        ));
    }

    @PostMapping("/actualizar")
    public ResponseEntity<?> actualizar(@AuthenticationPrincipal UserDetails ud,
                                        @RequestBody Map<String, String> body) {
        Usuario u = usuarioRepo.findByEmail(ud.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        try {
            Usuario.PlanSuscripcion plan = Usuario.PlanSuscripcion.valueOf(body.get("plan"));
            suscripcionService.actualizarPlan(u.getId(), plan);
            return ResponseEntity.ok(Map.of("mensaje", "Plan actualizado a " + plan));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Plan inválido"));
        }
    }

    @PostMapping("/cancelar")
    public ResponseEntity<?> cancelar(@AuthenticationPrincipal UserDetails ud) {
        Usuario u = usuarioRepo.findByEmail(ud.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        suscripcionService.cancelarPlan(u.getId());
        return ResponseEntity.ok(Map.of("mensaje", "Suscripción cancelada"));
    }
}
