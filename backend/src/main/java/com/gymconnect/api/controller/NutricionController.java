package com.gymconnect.api.controller;

import com.gymconnect.api.model.Entrenador;
import com.gymconnect.api.model.PlanNutricional;
import com.gymconnect.api.model.Relacion;
import com.gymconnect.api.model.Usuario;
import com.gymconnect.api.repository.EntrenadorRepository;
import com.gymconnect.api.repository.PlanNutricionalRepository;
import com.gymconnect.api.repository.RelacionRepository;
import com.gymconnect.api.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/nutricion")
@RequiredArgsConstructor
public class NutricionController {

    private final PlanNutricionalRepository planRepo;
    private final EntrenadorRepository entrenadorRepo;
    private final UsuarioRepository usuarioRepo;
    private final RelacionRepository relacionRepo;

    // Entrenador: listar planes que ha creado
    @GetMapping("/mis-planes")
    public ResponseEntity<List<PlanNutricional>> misPlanes(@AuthenticationPrincipal UserDetails ud) {
        Entrenador ent = entrenadorRepo.findByUsuarioEmail(ud.getUsername())
                .orElseThrow(() -> new RuntimeException("No encontrado"));
        return ResponseEntity.ok(planRepo.findByEntrenadorId(ent.getId()));
    }

    // Entrenador: planes de un cliente específico
    @GetMapping("/cliente/{clienteId}")
    public ResponseEntity<List<PlanNutricional>> planesDeCliente(@AuthenticationPrincipal UserDetails ud,
                                                                   @PathVariable Long clienteId) {
        Entrenador ent = entrenadorRepo.findByUsuarioEmail(ud.getUsername())
                .orElseThrow(() -> new RuntimeException("No encontrado"));
        return ResponseEntity.ok(planRepo.findByEntrenadorIdAndClienteId(ent.getId(), clienteId));
    }

    // Entrenador: crear plan para un cliente
    @PostMapping
    public ResponseEntity<PlanNutricional> crear(@AuthenticationPrincipal UserDetails ud,
                                                  @RequestBody Map<String, Object> body) {
        Entrenador ent = entrenadorRepo.findByUsuarioEmail(ud.getUsername())
                .orElseThrow(() -> new RuntimeException("No encontrado"));
        Long clienteId = ((Number) body.get("clienteId")).longValue();
        boolean esClienteActivo = relacionRepo.existsByClienteIdAndEntrenadorIdAndEstado(
                clienteId, ent.getId(), Relacion.Estado.ACTIVA);
        if (!esClienteActivo) return ResponseEntity.status(403).build();
        Usuario cliente = usuarioRepo.findById(clienteId)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));

        PlanNutricional p = new PlanNutricional();
        p.setEntrenador(ent);
        p.setCliente(cliente);
        p.setTitulo((String) body.get("titulo"));
        p.setDescripcion((String) body.get("descripcion"));
        p.setComidasJson((String) body.get("comidasJson"));
        if (body.get("caloriasTotal") != null) p.setCaloriasTotal(((Number) body.get("caloriasTotal")).intValue());
        if (body.get("proteinasG") != null) p.setProteinasG(((Number) body.get("proteinasG")).intValue());
        if (body.get("carbohidratosG") != null) p.setCarbohidratosG(((Number) body.get("carbohidratosG")).intValue());
        if (body.get("grasasG") != null) p.setGrasasG(((Number) body.get("grasasG")).intValue());

        return ResponseEntity.ok(planRepo.save(p));
    }

    // Entrenador: actualizar plan
    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(@AuthenticationPrincipal UserDetails ud,
                                         @PathVariable Long id,
                                         @RequestBody Map<String, Object> body) {
        Entrenador ent = entrenadorRepo.findByUsuarioEmail(ud.getUsername())
                .orElseThrow(() -> new RuntimeException("No encontrado"));
        return planRepo.findById(id)
                .filter(p -> p.getEntrenador().getId().equals(ent.getId()))
                .map(p -> {
                    if (body.containsKey("titulo")) p.setTitulo((String) body.get("titulo"));
                    if (body.containsKey("descripcion")) p.setDescripcion((String) body.get("descripcion"));
                    if (body.containsKey("comidasJson")) p.setComidasJson((String) body.get("comidasJson"));
                    if (body.get("caloriasTotal") != null) p.setCaloriasTotal(((Number) body.get("caloriasTotal")).intValue());
                    if (body.get("proteinasG") != null) p.setProteinasG(((Number) body.get("proteinasG")).intValue());
                    if (body.get("carbohidratosG") != null) p.setCarbohidratosG(((Number) body.get("carbohidratosG")).intValue());
                    if (body.get("grasasG") != null) p.setGrasasG(((Number) body.get("grasasG")).intValue());
                    p.setActualizadoEn(LocalDateTime.now());
                    return ResponseEntity.ok(planRepo.save(p));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // Entrenador: eliminar plan
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@AuthenticationPrincipal UserDetails ud, @PathVariable Long id) {
        Entrenador ent = entrenadorRepo.findByUsuarioEmail(ud.getUsername())
                .orElseThrow(() -> new RuntimeException("No encontrado"));
        return planRepo.findById(id)
                .filter(p -> p.getEntrenador().getId().equals(ent.getId()))
                .map(p -> { planRepo.delete(p); return ResponseEntity.ok().build(); })
                .orElse(ResponseEntity.notFound().build());
    }

    // Cliente: ver su plan nutricional más reciente
    @GetMapping("/mi-plan")
    public ResponseEntity<?> miPlan(@AuthenticationPrincipal UserDetails ud) {
        Usuario user = usuarioRepo.findByEmail(ud.getUsername())
                .orElseThrow(() -> new RuntimeException("No encontrado"));
        return planRepo.findTopByClienteIdOrderByActualizadoEnDesc(user.getId())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }
}
