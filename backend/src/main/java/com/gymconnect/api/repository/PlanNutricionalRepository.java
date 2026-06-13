package com.gymconnect.api.repository;

import com.gymconnect.api.model.PlanNutricional;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PlanNutricionalRepository extends JpaRepository<PlanNutricional, Long> {
    List<PlanNutricional> findByEntrenadorId(Long entrenadorId);
    List<PlanNutricional> findByEntrenadorIdAndClienteId(Long entrenadorId, Long clienteId);
    Optional<PlanNutricional> findTopByClienteIdOrderByActualizadoEnDesc(Long clienteId);
    Optional<PlanNutricional> findTopByEntrenadorIdAndClienteIdOrderByActualizadoEnDesc(Long entrenadorId, Long clienteId);
}
