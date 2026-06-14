package com.gymconnect.api.repository;

import com.gymconnect.api.model.Ejercicio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface EjercicioRepository extends JpaRepository<Ejercicio, Long> {
    // Ejercicios públicos del sistema + los creados por el entrenador
    @Query("SELECT e FROM Ejercicio e WHERE e.entrenador IS NULL OR e.entrenador.id = :entrenadorId ORDER BY e.grupoMuscular, e.nombre")
    List<Ejercicio> findPublicosYPropios(@Param("entrenadorId") Long entrenadorId);

    List<Ejercicio> findByEntrenadorIdOrderByNombre(Long entrenadorId);
    List<Ejercicio> findByEntrenadorIsNullOrderByGrupoMuscularAscNombreAsc();
}
