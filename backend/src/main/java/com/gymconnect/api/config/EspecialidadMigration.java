package com.gymconnect.api.config;

import com.gymconnect.api.controller.EntrenadorController;
import com.gymconnect.api.model.Entrenador;
import com.gymconnect.api.repository.EntrenadorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class EspecialidadMigration {

    private final EntrenadorRepository entrenadorRepo;

    // Mapeo de slugs antiguos y variaciones a la especialidad canónica
    private static final Map<String, String> SLUG_MAP = Map.ofEntries(
        Map.entry("musculacion",              "Musculación / Hipertrofia"),
        Map.entry("musculación",              "Musculación / Hipertrofia"),
        Map.entry("hipertrofia",              "Musculación / Hipertrofia"),
        Map.entry("perdida",                  "Pérdida de grasa"),
        Map.entry("pérdida",                  "Pérdida de grasa"),
        Map.entry("perdida de grasa",         "Pérdida de grasa"),
        Map.entry("nutricion",                "Nutrición deportiva"),
        Map.entry("nutrición",                "Nutrición deportiva"),
        Map.entry("nutricion deportiva",      "Nutrición deportiva"),
        Map.entry("fuerza",                   "Fuerza y potencia"),
        Map.entry("fuerza y potencia",        "Fuerza y potencia"),
        Map.entry("potencia",                 "Fuerza y potencia"),
        Map.entry("funcional",                "Entrenamiento funcional"),
        Map.entry("entrenamiento funcional",  "Entrenamiento funcional"),
        Map.entry("hiit",                     "HIIT / Cardio intenso"),
        Map.entry("cardio",                   "HIIT / Cardio intenso"),
        Map.entry("hiit / cardio intenso",    "HIIT / Cardio intenso"),
        Map.entry("crossfit",                 "Crossfit"),
        Map.entry("calistenia",               "Calistenia"),
        Map.entry("yoga",                     "Yoga"),
        Map.entry("pilates",                  "Pilates"),
        Map.entry("flexibilidad",             "Flexibilidad y movilidad"),
        Map.entry("movilidad",                "Flexibilidad y movilidad"),
        Map.entry("running",                  "Running / Atletismo"),
        Map.entry("atletismo",                "Running / Atletismo"),
        Map.entry("ciclismo",                 "Ciclismo"),
        Map.entry("natacion",                 "Natación"),
        Map.entry("natación",                 "Natación"),
        Map.entry("combate",                  "Deportes de combate"),
        Map.entry("deportes de combate",      "Deportes de combate"),
        Map.entry("rehabilitacion",           "Rehabilitación física"),
        Map.entry("rehabilitación",           "Rehabilitación física"),
        Map.entry("mayores",                  "Entrenamiento para mayores"),
        Map.entry("tercera edad",             "Entrenamiento para mayores"),
        Map.entry("preparacion",              "Preparación física deportiva"),
        Map.entry("preparación",              "Preparación física deportiva"),
        Map.entry("online",                   "Entrenamiento online"),
        Map.entry("entrenamiento online",     "Entrenamiento online"),
        Map.entry("postparto",                "Pérdida de peso post-parto"),
        Map.entry("post-parto",               "Pérdida de peso post-parto"),
        Map.entry("perdida post-parto",       "Pérdida de peso post-parto")
    );

    @Transactional
    @EventListener(ApplicationReadyEvent.class)
    public void migrarEspecialidades() {
        List<Entrenador> entrenadores = entrenadorRepo.findAll();
        int migrados = 0;

        for (Entrenador e : entrenadores) {
            List<String> originales = e.getEspecialidades();
            if (originales == null || originales.isEmpty()) continue;

            List<String> canonicas = new ArrayList<>();
            for (String esp : originales) {
                String canonical = mapear(esp);
                if (canonical != null && !canonicas.contains(canonical)) {
                    canonicas.add(canonical);
                }
            }

            // Solo actualizar si hay cambios
            if (!canonicas.equals(originales)) {
                e.setEspecialidades(canonicas);
                entrenadorRepo.save(e);
                migrados++;
                log.info("Migrated specialties for trainer {}: {} -> {}",
                    e.getId(), originales, canonicas);
            }
        }

        if (migrados > 0) {
            log.info("EspecialidadMigration: migrated {} trainers", migrados);
        }
    }

    private String mapear(String esp) {
        if (esp == null) return null;
        // Ya es canónica
        if (EntrenadorController.ESPECIALIDADES_VALIDAS.contains(esp)) return esp;
        // Buscar por slug/variación (case-insensitive)
        return SLUG_MAP.get(esp.toLowerCase().trim());
    }
}
