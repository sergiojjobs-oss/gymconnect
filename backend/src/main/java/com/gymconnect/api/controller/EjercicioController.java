package com.gymconnect.api.controller;

import com.gymconnect.api.model.Ejercicio;
import com.gymconnect.api.model.Entrenador;
import com.gymconnect.api.repository.EjercicioRepository;
import com.gymconnect.api.repository.EntrenadorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ejercicios")
@RequiredArgsConstructor
public class EjercicioController {

    private final EjercicioRepository ejercicioRepo;
    private final EntrenadorRepository entrenadorRepo;

    @GetMapping
    public ResponseEntity<List<Ejercicio>> getAll(@AuthenticationPrincipal UserDetails ud) {
        Entrenador ent = entrenadorRepo.findByUsuarioEmail(ud.getUsername())
                .orElseThrow(() -> new RuntimeException("No encontrado"));
        return ResponseEntity.ok(ejercicioRepo.findPublicosYPropios(ent.getId()));
    }

    @PostMapping
    public ResponseEntity<?> crear(@AuthenticationPrincipal UserDetails ud,
                                    @RequestBody Map<String, Object> body) {
        Entrenador ent = entrenadorRepo.findByUsuarioEmail(ud.getUsername())
                .orElseThrow(() -> new RuntimeException("No encontrado"));

        Ejercicio e = new Ejercicio();
        e.setNombre((String) body.get("nombre"));
        e.setGrupoMuscular((String) body.get("grupoMuscular"));
        e.setEquipamiento((String) body.get("equipamiento"));
        e.setDescripcion((String) body.get("descripcion"));
        e.setNivel((String) body.get("nivel"));
        e.setImagenUrl((String) body.get("imagenUrl"));
        e.setEntrenador(ent);

        return ResponseEntity.ok(ejercicioRepo.save(e));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(@AuthenticationPrincipal UserDetails ud,
                                         @PathVariable Long id,
                                         @RequestBody Map<String, Object> body) {
        Entrenador ent = entrenadorRepo.findByUsuarioEmail(ud.getUsername())
                .orElseThrow(() -> new RuntimeException("No encontrado"));
        return ejercicioRepo.findById(id)
                .filter(e -> e.getEntrenador() != null && e.getEntrenador().getId().equals(ent.getId()))
                .map(e -> {
                    if (body.get("nombre") != null) e.setNombre((String) body.get("nombre"));
                    if (body.get("grupoMuscular") != null) e.setGrupoMuscular((String) body.get("grupoMuscular"));
                    if (body.get("equipamiento") != null) e.setEquipamiento((String) body.get("equipamiento"));
                    if (body.get("descripcion") != null) e.setDescripcion((String) body.get("descripcion"));
                    if (body.get("nivel") != null) e.setNivel((String) body.get("nivel"));
                    if (body.get("imagenUrl") != null) e.setImagenUrl((String) body.get("imagenUrl"));
                    return ResponseEntity.ok(ejercicioRepo.save(e));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@AuthenticationPrincipal UserDetails ud, @PathVariable Long id) {
        Entrenador ent = entrenadorRepo.findByUsuarioEmail(ud.getUsername())
                .orElseThrow(() -> new RuntimeException("No encontrado"));
        return ejercicioRepo.findById(id)
                .filter(e -> e.getEntrenador() != null && e.getEntrenador().getId().equals(ent.getId()))
                .map(e -> { ejercicioRepo.delete(e); return ResponseEntity.ok().build(); })
                .orElse(ResponseEntity.notFound().build());
    }
}
