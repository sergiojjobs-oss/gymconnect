package com.gymconnect.api.controller;

import com.gymconnect.api.model.Entrenador;
import com.gymconnect.api.model.Relacion;
import com.gymconnect.api.model.Rutina;
import com.gymconnect.api.model.Usuario;
import com.gymconnect.api.repository.EntrenadorRepository;
import com.gymconnect.api.repository.RelacionRepository;
import com.gymconnect.api.repository.RutinaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/elite")
@RequiredArgsConstructor
public class EliteController {

    private final EntrenadorRepository entrenadorRepo;
    private final RelacionRepository relacionRepo;
    private final RutinaRepository rutinaRepo;

    private Entrenador getEliteEntrenador(UserDetails ud) {
        Entrenador ent = entrenadorRepo.findByUsuarioEmail(ud.getUsername())
                .orElseThrow(() -> new RuntimeException("No encontrado"));
        if (ent.getUsuario().getPlan() != Usuario.PlanSuscripcion.ELITE) {
            throw new SecurityException("Requiere plan ELITE");
        }
        return ent;
    }

    // ── Notas privadas por cliente ──────────────────────────────────────
    @GetMapping("/notas-privadas/{clienteId}")
    public ResponseEntity<?> getNotas(@AuthenticationPrincipal UserDetails ud,
                                       @PathVariable Long clienteId) {
        try {
            Entrenador ent = getEliteEntrenador(ud);
            return relacionRepo.findByEntrenadorIdAndEstado(ent.getId(), Relacion.Estado.ACTIVA)
                    .stream()
                    .filter(r -> r.getCliente().getId().equals(clienteId))
                    .findFirst()
                    .map(r -> ResponseEntity.ok(Map.of("notas", r.getNotasPrivadas() != null ? r.getNotasPrivadas() : "")))
                    .orElse(ResponseEntity.notFound().build());
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/notas-privadas/{clienteId}")
    public ResponseEntity<?> saveNotas(@AuthenticationPrincipal UserDetails ud,
                                        @PathVariable Long clienteId,
                                        @RequestBody Map<String, String> body) {
        try {
            Entrenador ent = getEliteEntrenador(ud);
            return relacionRepo.findByEntrenadorIdAndEstado(ent.getId(), Relacion.Estado.ACTIVA)
                    .stream()
                    .filter(r -> r.getCliente().getId().equals(clienteId))
                    .findFirst()
                    .map(r -> {
                        r.setNotasPrivadas(body.get("notas"));
                        relacionRepo.save(r);
                        return ResponseEntity.ok(Map.of("ok", true));
                    })
                    .orElse(ResponseEntity.notFound().build());
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(Map.of("error", e.getMessage()));
        }
    }

    // ── Plantillas de rutinas ───────────────────────────────────────────
    @GetMapping("/plantillas")
    public ResponseEntity<?> getPlantillas(@AuthenticationPrincipal UserDetails ud) {
        try {
            Entrenador ent = getEliteEntrenador(ud);
            List<Rutina> plantillas = rutinaRepo.findByEntrenadorIdAndEsPlantillaTrue(ent.getId());
            return ResponseEntity.ok(plantillas);
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/plantillas/{rutinaId}")
    public ResponseEntity<?> marcarPlantilla(@AuthenticationPrincipal UserDetails ud,
                                              @PathVariable Long rutinaId,
                                              @RequestBody Map<String, Boolean> body) {
        try {
            Entrenador ent = getEliteEntrenador(ud);
            return rutinaRepo.findById(rutinaId)
                    .filter(r -> r.getEntrenador().getId().equals(ent.getId()))
                    .map(r -> {
                        r.setEsPlantilla(body.getOrDefault("esPlantilla", true));
                        rutinaRepo.save(r);
                        return ResponseEntity.ok(Map.of("ok", true));
                    })
                    .orElse(ResponseEntity.notFound().build());
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(Map.of("error", e.getMessage()));
        }
    }
}
