package com.gymconnect.api.controller;

import com.gymconnect.api.model.Entrenador;
import com.gymconnect.api.model.ProgresoCliente;
import com.gymconnect.api.model.Relacion;
import com.gymconnect.api.model.Usuario;
import com.gymconnect.api.repository.EntrenadorRepository;
import com.gymconnect.api.repository.ProgresoClienteRepository;
import com.gymconnect.api.repository.RelacionRepository;
import com.gymconnect.api.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/progreso")
@RequiredArgsConstructor
public class ProgresoController {

    private final ProgresoClienteRepository progresoRepo;
    private final UsuarioRepository usuarioRepo;
    private final EntrenadorRepository entrenadorRepo;
    private final RelacionRepository relacionRepo;

    // Cliente: registrar check-in semanal
    @PostMapping("/check-in")
    public ResponseEntity<?> checkIn(@AuthenticationPrincipal UserDetails ud,
                                      @RequestBody Map<String, Object> body) {
        Usuario cliente = usuarioRepo.findByEmail(ud.getUsername())
                .orElseThrow(() -> new RuntimeException("No encontrado"));

        // Entrenador activo (opcional — el progreso es del cliente aunque no tenga entrenador)
        var relaciones = relacionRepo.findByClienteIdAndEstado(cliente.getId(), Relacion.Estado.ACTIVA);
        Entrenador entrenador = relaciones.isEmpty() ? null : relaciones.get(0).getEntrenador();

        ProgresoCliente p = new ProgresoCliente();
        p.setCliente(cliente);
        if (entrenador != null) p.setEntrenador(entrenador);
        p.setFecha(LocalDate.now());
        if (body.get("peso") != null) p.setPeso(((Number) body.get("peso")).doubleValue());
        if (body.get("grasaCorporal") != null) p.setGrasaCorporal(((Number) body.get("grasaCorporal")).doubleValue());
        if (body.get("musculatura") != null) p.setMusculatura(((Number) body.get("musculatura")).doubleValue());
        if (body.get("notas") != null) p.setNotas((String) body.get("notas"));
        if (body.get("fotoUrl") != null) p.setFotoUrl((String) body.get("fotoUrl"));
        if (body.get("energia") != null) p.setEnergia(((Number) body.get("energia")).intValue());
        if (body.get("cumplimiento") != null) p.setCumplimiento(((Number) body.get("cumplimiento")).intValue());

        return ResponseEntity.ok(progresoRepo.save(p));
    }

    // Cliente: ver su historial de progreso
    @GetMapping("/mi-historial")
    public ResponseEntity<List<ProgresoCliente>> miHistorial(@AuthenticationPrincipal UserDetails ud) {
        Usuario cliente = usuarioRepo.findByEmail(ud.getUsername())
                .orElseThrow(() -> new RuntimeException("No encontrado"));
        return ResponseEntity.ok(progresoRepo.findByClienteIdOrderByFechaDesc(cliente.getId()));
    }

    // Entrenador: ver progreso de un cliente específico
    @GetMapping("/cliente/{clienteId}")
    public ResponseEntity<List<ProgresoCliente>> progresoDeCliente(@AuthenticationPrincipal UserDetails ud,
                                                                     @PathVariable Long clienteId) {
        Entrenador ent = entrenadorRepo.findByUsuarioEmail(ud.getUsername())
                .orElseThrow(() -> new RuntimeException("No encontrado"));
        return ResponseEntity.ok(progresoRepo.findByEntrenadorIdAndClienteIdOrderByFechaDesc(ent.getId(), clienteId));
    }

    // Entrenador: añadir feedback a un check-in
    @PutMapping("/{id}/feedback")
    public ResponseEntity<?> feedback(@AuthenticationPrincipal UserDetails ud,
                                       @PathVariable Long id,
                                       @RequestBody Map<String, Object> body) {
        Entrenador ent = entrenadorRepo.findByUsuarioEmail(ud.getUsername())
                .orElseThrow(() -> new RuntimeException("No encontrado"));
        return progresoRepo.findById(id)
                .filter(p -> p.getEntrenador().getId().equals(ent.getId()))
                .map(p -> {
                    p.setNotasEntrenador((String) body.get("notasEntrenador"));
                    return ResponseEntity.ok(progresoRepo.save(p));
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
