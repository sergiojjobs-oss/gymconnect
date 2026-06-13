package com.gymconnect.api.repository;

import com.gymconnect.api.model.Mensaje;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface MensajeRepository extends JpaRepository<Mensaje, Long> {

    @Query("""
        SELECT m FROM Mensaje m
        WHERE (m.remitenteId = :a AND m.destinatarioId = :b)
           OR (m.remitenteId = :b AND m.destinatarioId = :a)
        ORDER BY m.fechaEnvio ASC
        """)
    List<Mensaje> findConversacion(Long a, Long b);

    long countByDestinatarioIdAndLeidoFalse(Long destinatarioId);
}
