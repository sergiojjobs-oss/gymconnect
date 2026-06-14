package com.gymconnect.api.controller;

import com.gymconnect.api.model.Entrenador;
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
import java.time.temporal.IsoFields;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/estadisticas")
@RequiredArgsConstructor
public class EstadisticasController {

    private final EntrenadorRepository entrenadorRepo;
    private final RelacionRepository relacionRepo;
    private final ProgresoClienteRepository progresoRepo;
    private final UsuarioRepository usuarioRepo;

    @GetMapping("/elite")
    public ResponseEntity<?> eliteStats(@AuthenticationPrincipal UserDetails ud) {
        Entrenador ent = entrenadorRepo.findByUsuarioEmail(ud.getUsername())
                .orElseThrow(() -> new RuntimeException("No encontrado"));

        if (ent.getUsuario().getPlan() != Usuario.PlanSuscripcion.ELITE) {
            return ResponseEntity.status(403).body(Map.of("error", "Requiere plan ELITE"));
        }

        Long entId = ent.getId();

        // Clientes activos
        var relaciones = relacionRepo.findByEntrenadorIdAndEstado(entId, Relacion.Estado.ACTIVA);
        List<Long> clienteIds = relaciones.stream().map(r -> r.getCliente().getId()).toList();

        // Todos los check-ins de sus clientes
        var todosProgreso = progresoRepo.findByEntrenadorId(entId);

        // ── KPIs ──────────────────────────────────────────────
        long totalClientes = clienteIds.size();

        double cumplimientoMedio = todosProgreso.stream()
                .filter(p -> p.getCumplimiento() != null)
                .mapToInt(p -> p.getCumplimiento())
                .average().orElse(0);

        double energiamedia = todosProgreso.stream()
                .filter(p -> p.getEnergia() != null)
                .mapToInt(p -> p.getEnergia())
                .average().orElse(0);

        long totalCheckins = todosProgreso.size();

        // ── Check-ins por semana (últimas 8 semanas) ──────────
        Map<String, Long> checkinsPorSemana = new LinkedHashMap<>();
        LocalDate hoy = LocalDate.now();
        for (int i = 7; i >= 0; i--) {
            LocalDate semana = hoy.minusWeeks(i);
            int year = semana.getYear();
            int week = semana.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR);
            checkinsPorSemana.put(year + "-W" + String.format("%02d", week), 0L);
        }
        todosProgreso.forEach(p -> {
            LocalDate fecha = p.getFecha() != null ? p.getFecha() : p.getCreadoEn().toLocalDate();
            if (!fecha.isBefore(hoy.minusWeeks(8))) {
                int year = fecha.getYear();
                int week = fecha.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR);
                String key = year + "-W" + String.format("%02d", week);
                checkinsPorSemana.merge(key, 1L, Long::sum);
            }
        });

        // ── Evolución peso promedio por semana ────────────────
        Map<String, List<Double>> pesosPorSemana = new LinkedHashMap<>();
        checkinsPorSemana.keySet().forEach(k -> pesosPorSemana.put(k, new ArrayList<>()));
        todosProgreso.stream()
                .filter(p -> p.getPeso() != null)
                .forEach(p -> {
                    LocalDate fecha = p.getFecha() != null ? p.getFecha() : p.getCreadoEn().toLocalDate();
                    if (!fecha.isBefore(hoy.minusWeeks(8))) {
                        String key = fecha.getYear() + "-W" + String.format("%02d", fecha.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR));
                        if (pesosPorSemana.containsKey(key)) pesosPorSemana.get(key).add(p.getPeso());
                    }
                });
        Map<String, Double> pesoPorSemana = new LinkedHashMap<>();
        pesosPorSemana.forEach((k, v) -> pesoPorSemana.put(k, v.isEmpty() ? null : v.stream().mapToDouble(d -> d).average().orElse(0)));

        // ── Distribución de energía ───────────────────────────
        Map<Integer, Long> distribucionEnergia = todosProgreso.stream()
                .filter(p -> p.getEnergia() != null)
                .collect(Collectors.groupingBy(p -> p.getEnergia(), Collectors.counting()));

        // ── Top clientes por actividad ────────────────────────
        Map<Long, Long> checkinsPorCliente = todosProgreso.stream()
                .collect(Collectors.groupingBy(p -> p.getCliente().getId(), Collectors.counting()));

        List<Map<String, Object>> topClientes = checkinsPorCliente.entrySet().stream()
                .sorted(Map.Entry.<Long, Long>comparingByValue().reversed())
                .limit(5)
                .map(e -> {
                    var cliente = usuarioRepo.findById(e.getKey()).orElse(null);
                    if (cliente == null) return null;
                    // Último peso
                    var ultimoProgreso = todosProgreso.stream()
                            .filter(p -> p.getCliente().getId().equals(e.getKey()) && p.getPeso() != null)
                            .max(Comparator.comparing(p -> p.getFecha() != null ? p.getFecha() : p.getCreadoEn().toLocalDate()));
                    // Progreso de peso
                    var primerProgreso = todosProgreso.stream()
                            .filter(p -> p.getCliente().getId().equals(e.getKey()) && p.getPeso() != null)
                            .min(Comparator.comparing(p -> p.getFecha() != null ? p.getFecha() : p.getCreadoEn().toLocalDate()));

                    Double pesoActual = ultimoProgreso.map(p -> p.getPeso()).orElse(null);
                    Double pesoInicial = primerProgreso.map(p -> p.getPeso()).orElse(null);
                    Double deltaPeso = (pesoActual != null && pesoInicial != null) ? pesoActual - pesoInicial : null;

                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("id", e.getKey());
                    m.put("nombre", cliente.getNombre() + " " + cliente.getApellido());
                    m.put("checkins", e.getValue());
                    m.put("pesoActual", pesoActual);
                    m.put("deltaPeso", deltaPeso);
                    m.put("cumplimientoMedio", todosProgreso.stream()
                            .filter(p -> p.getCliente().getId().equals(e.getKey()) && p.getCumplimiento() != null)
                            .mapToInt(p -> p.getCumplimiento()).average().orElse(0));
                    return m;
                })
                .filter(Objects::nonNull)
                .toList();

        return ResponseEntity.ok(Map.of(
                "kpis", Map.of(
                        "totalClientes", totalClientes,
                        "totalCheckins", totalCheckins,
                        "cumplimientoMedio", Math.round(cumplimientoMedio * 10.0) / 10.0,
                        "energiaMedia", Math.round(energiamedia * 10.0) / 10.0
                ),
                "checkinsPorSemana", checkinsPorSemana,
                "pesoPorSemana", pesoPorSemana,
                "distribucionEnergia", distribucionEnergia,
                "topClientes", topClientes
        ));
    }
}
