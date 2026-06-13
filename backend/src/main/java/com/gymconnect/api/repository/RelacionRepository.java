package com.gymconnect.api.repository;

import com.gymconnect.api.model.Relacion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RelacionRepository extends JpaRepository<Relacion, Long> {
    List<Relacion> findByEntrenadorIdAndEstado(Long entrenadorId, Relacion.Estado estado);
    long countByEntrenadorIdAndEstado(Long entrenadorId, Relacion.Estado estado);
    boolean existsByClienteIdAndEntrenadorIdAndEstado(Long clienteId, Long entrenadorId, Relacion.Estado estado);
}
