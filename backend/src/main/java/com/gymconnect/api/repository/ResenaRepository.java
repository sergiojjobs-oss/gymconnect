package com.gymconnect.api.repository;

import com.gymconnect.api.model.Resena;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ResenaRepository extends JpaRepository<Resena, Long> {
    List<Resena> findByEntrenadorIdOrderByCreadaEnDesc(Long entrenadorId);
    Optional<Resena> findByClienteIdAndEntrenadorId(Long clienteId, Long entrenadorId);
}
