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

    // Mapeo de especialidades antiguas (no-gimnasio y variantes) a las nuevas canónicas
    private static final Map<String, String> SLUG_MAP = Map.ofEntries(
        // Musculación / Hipertrofia (nombre anterior con barra y espacio)
        Map.entry("musculación / hipertrofia",     "Musculación/Hipertrofia"),
        Map.entry("musculacion / hipertrofia",     "Musculación/Hipertrofia"),
        Map.entry("musculación",                   "Musculación/Hipertrofia"),
        Map.entry("musculacion",                   "Musculación/Hipertrofia"),
        Map.entry("hipertrofia",                   "Musculación/Hipertrofia"),
        Map.entry("ganancia muscular",             "Ganancia de masa muscular"),
        Map.entry("masa muscular",                 "Ganancia de masa muscular"),
        // Fuerza
        Map.entry("fuerza y potencia",             "Fuerza y potencia"),
        Map.entry("fuerza",                        "Fuerza y potencia"),
        Map.entry("potencia",                      "Fuerza y potencia"),
        // HIIT / Cardio (nombre anterior)
        Map.entry("hiit / cardio intenso",         "HIIT en sala"),
        Map.entry("hiit/cardio intenso",           "HIIT en sala"),
        Map.entry("hiit",                          "HIIT en sala"),
        Map.entry("cardio",                        "Cardio de gimnasio"),
        Map.entry("cardio intenso",                "HIIT en sala"),
        // Nutrición
        Map.entry("nutrición deportiva",           "Nutrición deportiva"),
        Map.entry("nutricion deportiva",           "Nutrición deportiva"),
        Map.entry("nutrición",                     "Nutrición deportiva"),
        Map.entry("nutricion",                     "Nutrición deportiva"),
        // Pérdida de grasa
        Map.entry("pérdida de grasa",              "Pérdida de grasa"),
        Map.entry("perdida de grasa",              "Pérdida de grasa"),
        Map.entry("perdida",                       "Pérdida de grasa"),
        Map.entry("pérdida",                       "Pérdida de grasa"),
        // Flexibilidad y movilidad
        Map.entry("flexibilidad y movilidad",      "Flexibilidad y movilidad"),
        Map.entry("flexibilidad",                  "Flexibilidad y movilidad"),
        Map.entry("movilidad",                     "Flexibilidad y movilidad"),
        Map.entry("movilidad y estiramientos",     "Flexibilidad y movilidad"),
        // Calistenia
        Map.entry("calistenia",                    "Calistenia en sala"),
        Map.entry("calistenia en sala",            "Calistenia en sala"),
        // Rehabilitación
        Map.entry("rehabilitación física",         "Rehabilitación física"),
        Map.entry("rehabilitacion fisica",         "Rehabilitación física"),
        Map.entry("rehabilitación",                "Rehabilitación física"),
        Map.entry("rehabilitacion",                "Rehabilitación física"),
        // Mayores
        Map.entry("entrenamiento para mayores",    "Entrenamiento para mayores"),
        Map.entry("tercera edad",                  "Entrenamiento para mayores"),
        // Online
        Map.entry("entrenamiento online",          "Entrenamiento online"),
        Map.entry("online",                        "Entrenamiento online"),
        // Post-parto
        Map.entry("pérdida de peso post-parto",    "Pérdida de peso post-parto"),
        Map.entry("perdida de peso post-parto",    "Pérdida de peso post-parto"),
        Map.entry("postparto",                     "Pérdida de peso post-parto"),
        Map.entry("post-parto",                    "Pérdida de peso post-parto"),
        // Funcional → Entrenamiento en máquinas (lo más cercano en gimnasio)
        Map.entry("entrenamiento funcional",       "Entrenamiento en máquinas"),
        Map.entry("funcional",                     "Entrenamiento en máquinas"),
        // No-gimnasio → equivalente gym o descartadas
        Map.entry("crossfit",                      "HIIT en sala"),
        Map.entry("yoga",                          "Flexibilidad y movilidad"),
        Map.entry("pilates",                       "Core y abdomen"),
        Map.entry("running / atletismo",           "Cardio de gimnasio"),
        Map.entry("running",                       "Cardio de gimnasio"),
        Map.entry("atletismo",                     "Cardio de gimnasio"),
        Map.entry("ciclismo",                      "Cardio de gimnasio"),
        Map.entry("natación",                      "Cardio de gimnasio"),
        Map.entry("natacion",                      "Cardio de gimnasio"),
        Map.entry("deportes de combate",           "Fuerza y potencia"),
        Map.entry("combate",                       "Fuerza y potencia"),
        Map.entry("preparación física deportiva",  "Fuerza y potencia"),
        Map.entry("preparacion fisica deportiva",  "Fuerza y potencia"),
        Map.entry("preparación",                   "Fuerza y potencia"),
        Map.entry("preparacion",                   "Fuerza y potencia")
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
                // Si no tiene mapeo, se descarta (no es de gimnasio y no hay equivalente)
            }

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
        } else {
            log.info("EspecialidadMigration: all trainers already up to date");
        }
    }

    private String mapear(String esp) {
        if (esp == null) return null;
        // Ya es canónica (nueva lista gym)
        if (EntrenadorController.ESPECIALIDADES_VALIDAS.contains(esp)) return esp;
        // Buscar por slug/variación (case-insensitive)
        return SLUG_MAP.get(esp.toLowerCase().trim());
    }
}
