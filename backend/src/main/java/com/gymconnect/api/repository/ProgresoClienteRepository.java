package com.gymconnect.api.repository;

import com.gymconnect.api.model.ProgresoCliente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProgresoClienteRepository extends JpaRepository<ProgresoCliente, Long> {
    List<ProgresoCliente> findByClienteIdOrderByFechaDesc(Long clienteId);
    List<ProgresoCliente> findByEntrenadorIdAndClienteIdOrderByFechaDesc(Long entrenadorId, Long clienteId);
    Optional<ProgresoCliente> findTopByClienteIdOrderByFechaDesc(Long clienteId);
}
