package com.gymconnect.api.repository;

import com.gymconnect.api.model.Mensaje;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

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

    List<Mensaje> findByRemitenteIdAndDestinatarioIdAndLeidoFalse(Long remitenteId, Long destinatarioId);

    @Query("""
        SELECT DISTINCT CASE
            WHEN m.remitenteId = :userId THEN m.destinatarioId
            ELSE m.remitenteId
        END
        FROM Mensaje m
        WHERE m.remitenteId = :userId OR m.destinatarioId = :userId
        """)
    List<Long> findInterlocutorIds(Long userId);

    @Query("SELECT COUNT(DISTINCT m.remitenteId) FROM Mensaje m WHERE m.destinatarioId IN (SELECT e.usuario.id FROM Entrenador e)")
    long countClientesConMensajes();

    @Transactional
    void deleteByRemitenteIdAndDestinatarioId(Long remitenteId, Long destinatarioId);
}
