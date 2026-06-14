package com.gymconnect.api.controller;

import com.gymconnect.api.model.Entrenador;
import com.gymconnect.api.model.Sesion;
import com.gymconnect.api.model.Usuario;
import com.gymconnect.api.repository.EntrenadorRepository;
import com.gymconnect.api.repository.SesionRepository;
import com.gymconnect.api.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/sesiones")
@RequiredArgsConstructor
public class SesionController {

    private final SesionRepository sesionRepo;
    private final EntrenadorRepository entrenadorRepo;
    private final UsuarioRepository usuarioRepo;

    @GetMapping
    public ResponseEntity<List<Sesion>> getMisSesiones(@AuthenticationPrincipal UserDetails ud) {
        Entrenador ent = entrenadorRepo.findByUsuarioEmail(ud.getUsername())
                .orElseThrow(() -> new RuntimeException("No encontrado"));
        return ResponseEntity.ok(sesionRepo.findByEntrenadorIdOrderByFechaAscHoraAsc(ent.getId()));
    }

    @GetMapping("/semana")
    public ResponseEntity<List<Sesion>> getSemana(@AuthenticationPrincipal UserDetails ud,
                                                    @RequestParam(required = false) String desde,
                                                    @RequestParam(required = false) String hasta) {
        Entrenador ent = entrenadorRepo.findByUsuarioEmail(ud.getUsername())
                .orElseThrow(() -> new RuntimeException("No encontrado"));
        LocalDate d = desde != null ? LocalDate.parse(desde) : LocalDate.now().minusDays(LocalDate.now().getDayOfWeek().getValue() - 1);
        LocalDate h = hasta != null ? LocalDate.parse(hasta) : d.plusDays(6);
        return ResponseEntity.ok(sesionRepo.findByEntrenadorIdAndFechaBetweenOrderByFechaAscHoraAsc(ent.getId(), d, h));
    }

    // Cliente: ver sus propias sesiones
    @GetMapping("/mis-sesiones")
    public ResponseEntity<List<Sesion>> getMisSesionesCliente(@AuthenticationPrincipal UserDetails ud) {
        Usuario u = usuarioRepo.findByEmail(ud.getUsername()).orElseThrow();
        return ResponseEntity.ok(sesionRepo.findByClienteIdOrderByFechaAscHoraAsc(u.getId()));
    }

    @PostMapping
    public ResponseEntity<?> crear(@AuthenticationPrincipal UserDetails ud,
                                    @RequestBody Map<String, Object> body) {
        Entrenador ent = entrenadorRepo.findByUsuarioEmail(ud.getUsername())
                .orElseThrow(() -> new RuntimeException("No encontrado"));
        Long clienteId = Long.parseLong(body.get("clienteId").toString());
        Usuario cliente = usuarioRepo.findById(clienteId)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));

        Sesion s = new Sesion();
        s.setEntrenador(ent);
        s.setCliente(cliente);
        s.setFecha(LocalDate.parse((String) body.get("fecha")));
        if (body.get("hora") != null) s.setHora(LocalTime.parse((String) body.get("hora")));
        if (body.get("duracionMinutos") != null) s.setDuracionMinutos(((Number) body.get("duracionMinutos")).intValue());
        if (body.get("tipo") != null) s.setTipo((String) body.get("tipo"));
        if (body.get("notas") != null) s.setNotas((String) body.get("notas"));

        return ResponseEntity.ok(sesionRepo.save(s));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(@AuthenticationPrincipal UserDetails ud,
                                         @PathVariable Long id,
                                         @RequestBody Map<String, Object> body) {
        Entrenador ent = entrenadorRepo.findByUsuarioEmail(ud.getUsername())
                .orElseThrow(() -> new RuntimeException("No encontrado"));
        return sesionRepo.findById(id)
                .filter(s -> s.getEntrenador().getId().equals(ent.getId()))
                .map(s -> {
                    if (body.get("fecha") != null) s.setFecha(LocalDate.parse((String) body.get("fecha")));
                    if (body.get("hora") != null) s.setHora(LocalTime.parse((String) body.get("hora")));
                    if (body.get("duracionMinutos") != null) s.setDuracionMinutos(((Number) body.get("duracionMinutos")).intValue());
                    if (body.get("tipo") != null) s.setTipo((String) body.get("tipo"));
                    if (body.get("notas") != null) s.setNotas((String) body.get("notas"));
                    if (body.get("estado") != null) s.setEstado(Sesion.Estado.valueOf((String) body.get("estado")));
                    return ResponseEntity.ok(sesionRepo.save(s));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@AuthenticationPrincipal UserDetails ud, @PathVariable Long id) {
        Entrenador ent = entrenadorRepo.findByUsuarioEmail(ud.getUsername())
                .orElseThrow(() -> new RuntimeException("No encontrado"));
        return sesionRepo.findById(id)
                .filter(s -> s.getEntrenador().getId().equals(ent.getId()))
                .map(s -> { sesionRepo.delete(s); return ResponseEntity.ok().build(); })
                .orElse(ResponseEntity.notFound().build());
    }
}
