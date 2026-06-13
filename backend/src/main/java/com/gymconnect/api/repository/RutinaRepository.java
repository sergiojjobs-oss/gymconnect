package com.gymconnect.api.repository;

import com.gymconnect.api.model.Rutina;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RutinaRepository extends JpaRepository<Rutina, Long> {
    List<Rutina> findByEntrenadorIdAndActivaTrue(Long entrenadorId);
    List<Rutina> findByEntrenadorIdAndClienteIsNullAndActivaTrue(Long entrenadorId);
    List<Rutina> findByEntrenadorIdAndClienteIdAndActivaTrue(Long entrenadorId, Long clienteId);
    Optional<Rutina> findTopByEntrenadorIdAndClienteIdAndActivaTrueOrderByCreadaEnDesc(Long entrenadorId, Long clienteId);
    // Rutina asignada a este cliente (por cualquier entrenador activo)
    Optional<Rutina> findTopByClienteIdAndActivaTrueOrderByCreadaEnDesc(Long clienteId);
}
