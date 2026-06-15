package com.gymconnect.api.controller;

import com.gymconnect.api.dto.EntrenadorDto;
import com.gymconnect.api.model.Entrenador;
import com.gymconnect.api.model.Relacion;
import com.gymconnect.api.repository.EntrenadorRepository;
import com.gymconnect.api.repository.MensajeRepository;
import com.gymconnect.api.repository.RelacionRepository;
import com.gymconnect.api.service.EntrenadorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/entrenadores")
@RequiredArgsConstructor
public class EntrenadorController {

    public static final Set<String> ESPECIALIDADES_VALIDAS = Set.of(
        "Musculación / Hipertrofia",
        "Pérdida de grasa",
        "Nutrición deportiva",
        "Fuerza y potencia",
        "Entrenamiento funcional",
        "HIIT / Cardio intenso",
        "Crossfit",
        "Calistenia",
        "Yoga",
        "Pilates",
        "Flexibilidad y movilidad",
        "Running / Atletismo",
        "Ciclismo",
        "Natación",
        "Deportes de combate",
        "Rehabilitación física",
        "Entrenamiento para mayores",
        "Preparación física deportiva",
        "Entrenamiento online",
        "Pérdida de peso post-parto"
    );

    private final EntrenadorService service;
    private final EntrenadorRepository entrenadorRepo;
    private final RelacionRepository relacionRepo;
    private final MensajeRepository mensajeRepo;

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

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> stats() {
        long totalEntrenadores = entrenadorRepo.count();
        long clientesPagadores = relacionRepo.countByEstado(com.gymconnect.api.model.Relacion.Estado.ACTIVA);
        long clientesMensajes = mensajeRepo.countClientesConMensajes();
        long totalClientes = Math.max(clientesPagadores, clientesMensajes);
        double avgRating = entrenadorRepo.findAll().stream()
                .mapToDouble(e -> e.getRating() != null ? e.getRating() : 5.0)
                .average().orElse(5.0);
        return ResponseEntity.ok(Map.of(
                "entrenadores", totalEntrenadores,
                "clientesActivos", totalClientes,
                "valoracionMedia", Math.round(avgRating * 10.0) / 10.0
        ));
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
        if (body.containsKey("especialidades")) {
            List<String> raw = (List<String>) body.get("especialidades");
            List<String> validas = raw.stream()
                .filter(ESPECIALIDADES_VALIDAS::contains)
                .distinct()
                .limit(6)
                .toList();
            e.setEspecialidades(validas);
        }
        if (body.containsKey("servicios"))         e.setServicios((List<String>) body.get("servicios"));
        if (body.containsKey("paypalEmail"))       e.setPaypalEmail((String) body.get("paypalEmail"));
        if (body.containsKey("fotoUrl"))           e.setFotoUrl((String) body.get("fotoUrl"));
        if (body.containsKey("metodologia"))       e.setMetodologia((String) body.get("metodologia"));
        if (body.containsKey("instagram"))         e.setInstagram((String) body.get("instagram"));
        if (body.containsKey("youtube"))           e.setYoutube((String) body.get("youtube"));
        if (body.containsKey("certificaciones"))   e.setCertificaciones((List<String>) body.get("certificaciones"));
        if (body.containsKey("idiomas"))           e.setIdiomas((List<String>) body.get("idiomas"));

        entrenadorRepo.save(e);
        return ResponseEntity.ok(EntrenadorDto.from(e));
    }
}
