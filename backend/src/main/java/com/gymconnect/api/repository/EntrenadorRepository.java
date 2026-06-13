package com.gymconnect.api.repository;

import com.gymconnect.api.model.Entrenador;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EntrenadorRepository extends JpaRepository<Entrenador, Long> {

    Optional<Entrenador> findByUsuarioId(Long usuarioId);

    @Query("SELECT e FROM Entrenador e WHERE " +
           "(:especialidad IS NULL OR :especialidad MEMBER OF e.especialidades) AND " +
           "(:precioMax IS NULL OR e.precioMensual <= :precioMax) AND " +
           "(:precioMin IS NULL OR e.precioMensual >= :precioMin) AND " +
           "(:ratingMin IS NULL OR e.rating >= :ratingMin)")
    List<Entrenador> buscarConFiltros(
        @Param("especialidad") String especialidad,
        @Param("precioMin") Double precioMin,
        @Param("precioMax") Double precioMax,
        @Param("ratingMin") Double ratingMin
    );
}
