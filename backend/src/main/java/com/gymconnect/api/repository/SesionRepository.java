package com.gymconnect.api.repository;

import com.gymconnect.api.model.Sesion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface SesionRepository extends JpaRepository<Sesion, Long> {
    List<Sesion> findByEntrenadorIdOrderByFechaAscHoraAsc(Long entrenadorId);
    List<Sesion> findByEntrenadorIdAndFechaBetweenOrderByFechaAscHoraAsc(Long entrenadorId, LocalDate desde, LocalDate hasta);
    List<Sesion> findByClienteIdOrderByFechaAscHoraAsc(Long clienteId);
}
