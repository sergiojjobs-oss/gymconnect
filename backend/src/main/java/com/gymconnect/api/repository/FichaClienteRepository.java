package com.gymconnect.api.repository;

import com.gymconnect.api.model.FichaCliente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FichaClienteRepository extends JpaRepository<FichaCliente, Long> {
    Optional<FichaCliente> findByClienteIdAndEntrenadorId(Long clienteId, Long entrenadorId);
    Optional<FichaCliente> findByClienteId(Long clienteId);
}
