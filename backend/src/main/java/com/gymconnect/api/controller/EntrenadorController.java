package com.gymconnect.api.controller;

import com.gymconnect.api.dto.EntrenadorDto;
import com.gymconnect.api.model.Entrenador;
import com.gymconnect.api.repository.EntrenadorRepository;
import com.gymconnect.api.service.EntrenadorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/entrenadores")
@RequiredArgsConstructor
public class EntrenadorController {

    private final EntrenadorService service;
    private final EntrenadorRepository entrenadorRepo;

    @GetMapping
    public ResponseEntity<List<EntrenadorDto>> buscar(
            @RequestParam(required = false) String especialidad,
            @RequestParam(required = false) Double precioMin,
            @RequestParam(required = false) Double precioMax,
            @RequestParam(required = false) Double ratingMin) {
        return ResponseEntity.ok(service.buscar(especialidad, precioMin, precioMax, ratingMin)
                .stream().map(EntrenadorDto::from).toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(EntrenadorDto.from(service.getById(id)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/mi-perfil")
    public ResponseEntity<?> miPerfil(@AuthenticationPrincipal UserDetails ud) {
        return entrenadorRepo.findByUsuarioEmail(ud.getUsername())
                .map(e -> ResponseEntity.ok(EntrenadorDto.from(e)))
                .orElse(ResponseEntity.notFound().build());
    }

    @SuppressWarnings("unchecked")
    @PutMapping("/mi-perfil")
    public ResponseEntity<?> actualizarMiPerfil(@AuthenticationPrincipal UserDetails ud,
                                                 @RequestBody Map<String, Object> body) {
        Entrenador e = entrenadorRepo.findByUsuarioEmail(ud.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("Entrenador no encontrado"));

        if (body.containsKey("bio"))              e.setBio((String) body.get("bio"));
        if (body.containsKey("ciudad"))           e.setCiudad((String) body.get("ciudad"));
        if (body.containsKey("precioMensual"))    e.setPrecioMensual(((Number) body.get("precioMensual")).doubleValue());
        if (body.containsKey("aniosExperiencia")) e.setAniosExperiencia(((Number) body.get("aniosExperiencia")).intValue());
        if (body.containsKey("especialidades"))   e.setEspecialidades((List<String>) body.get("especialidades"));
        if (body.containsKey("servicios"))        e.setServicios((List<String>) body.get("servicios"));

        entrenadorRepo.save(e);
        return ResponseEntity.ok(EntrenadorDto.from(e));
    }
}
