package com.gymconnect.api.controller;

import com.gymconnect.api.model.Entrenador;
import com.gymconnect.api.model.Relacion;
import com.gymconnect.api.model.Rutina;
import com.gymconnect.api.model.Usuario;
import com.gymconnect.api.repository.EntrenadorRepository;
import com.gymconnect.api.repository.RelacionRepository;
import com.gymconnect.api.repository.RutinaRepository;
import com.gymconnect.api.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/rutinas")
@RequiredArgsConstructor
public class RutinaController {

    private final RutinaRepository rutinaRepo;
    private final EntrenadorRepository entrenadorRepo;
    private final UsuarioRepository usuarioRepo;
    private final RelacionRepository relacionRepo;

    // Entrenador: listar todas sus rutinas
    @GetMapping("/mis-rutinas")
    public ResponseEntity<List<Rutina>> misRutinas(@AuthenticationPrincipal UserDetails ud) {
        Entrenador ent = entrenadorRepo.findByUsuarioEmail(ud.getUsername())
                .orElseThrow(() -> new RuntimeException("No encontrado"));
        return ResponseEntity.ok(rutinaRepo.findByEntrenadorIdAndActivaTrue(ent.getId()));
    }

    // Entrenador: crear rutina (genérica o para un cliente)
    @PostMapping
    public ResponseEntity<Rutina> crear(@AuthenticationPrincipal UserDetails ud,
                                         @RequestBody Map<String, Object> body) {
        Entrenador ent = entrenadorRepo.findByUsuarioEmail(ud.getUsername())
                .orElseThrow(() -> new RuntimeException("No encontrado"));

        Rutina r = new Rutina();
        r.setEntrenador(ent);
        r.setTitulo((String) body.get("titulo"));
        r.setDescripcion((String) body.get("descripcion"));
        r.setEjerciciosJson((String) body.get("ejerciciosJson"));
        r.setDiasSemana((String) body.get("diasSemana"));
        r.setNivel((String) body.get("nivel"));
        if (body.get("duracionMinutos") != null)
            r.setDuracionMinutos(((Number) body.get("duracionMinutos")).intValue());

        // Si se especifica clienteId, validar que es cliente activo de este entrenador
        if (body.get("clienteId") != null) {
            Long clienteId = ((Number) body.get("clienteId")).longValue();
            boolean esClienteActivo = relacionRepo.existsByClienteIdAndEntrenadorIdAndEstado(
                    clienteId, ent.getId(), Relacion.Estado.ACTIVA);
            if (!esClienteActivo)
                return ResponseEntity.status(403).build();
            usuarioRepo.findById(clienteId).ifPresent(r::setCliente);
        }

        return ResponseEntity.ok(rutinaRepo.save(r));
    }

    // Entrenador: actualizar rutina
    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(@AuthenticationPrincipal UserDetails ud,
                                         @PathVariable Long id,
                                         @RequestBody Map<String, Object> body) {
        Entrenador ent = entrenadorRepo.findByUsuarioEmail(ud.getUsername())
                .orElseThrow(() -> new RuntimeException("No encontrado"));

        return rutinaRepo.findById(id)
                .filter(r -> r.getEntrenador().getId().equals(ent.getId()))
                .map(r -> {
                    if (body.containsKey("titulo")) r.setTitulo((String) body.get("titulo"));
                    if (body.containsKey("descripcion")) r.setDescripcion((String) body.get("descripcion"));
                    if (body.containsKey("ejerciciosJson")) r.setEjerciciosJson((String) body.get("ejerciciosJson"));
                    if (body.containsKey("diasSemana")) r.setDiasSemana((String) body.get("diasSemana"));
                    if (body.containsKey("nivel")) r.setNivel((String) body.get("nivel"));
                    if (body.containsKey("duracionMinutos") && body.get("duracionMinutos") != null)
                        r.setDuracionMinutos(((Number) body.get("duracionMinutos")).intValue());
                    if (body.containsKey("clienteId")) {
                        if (body.get("clienteId") == null) {
                            r.setCliente(null);
                        } else {
                            Long clienteId = ((Number) body.get("clienteId")).longValue();
                            usuarioRepo.findById(clienteId).ifPresent(r::setCliente);
                        }
                    }
                    return ResponseEntity.ok(rutinaRepo.save(r));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // Entrenador: eliminar (desactivar) rutina
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@AuthenticationPrincipal UserDetails ud, @PathVariable Long id) {
        Entrenador ent = entrenadorRepo.findByUsuarioEmail(ud.getUsername())
                .orElseThrow(() -> new RuntimeException("No encontrado"));
        return rutinaRepo.findById(id)
                .filter(r -> r.getEntrenador().getId().equals(ent.getId()))
                .map(r -> { r.setActiva(false); rutinaRepo.save(r); return ResponseEntity.ok().build(); })
                .orElse(ResponseEntity.notFound().build());
    }

    // Entrenador: rutinas de un cliente específico
    @GetMapping("/cliente/{clienteId}")
    public ResponseEntity<List<Rutina>> rutinasDeCliente(@AuthenticationPrincipal UserDetails ud,
                                                          @PathVariable Long clienteId) {
        Entrenador ent = entrenadorRepo.findByUsuarioEmail(ud.getUsername())
                .orElseThrow(() -> new RuntimeException("No encontrado"));
        return ResponseEntity.ok(rutinaRepo.findByEntrenadorIdAndClienteIdAndActivaTrue(ent.getId(), clienteId));
    }

    // Cliente: ver su rutina asignada
    @GetMapping("/mi-rutina")
    public ResponseEntity<?> miRutina(@AuthenticationPrincipal UserDetails ud) {
        Usuario user = usuarioRepo.findByEmail(ud.getUsername())
                .orElseThrow(() -> new RuntimeException("No encontrado"));
        return rutinaRepo.findTopByClienteIdAndActivaTrueOrderByCreadaEnDesc(user.getId())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }
}
