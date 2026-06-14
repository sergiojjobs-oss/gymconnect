package com.gymconnect.api.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "rutinas")
@Data
@NoArgsConstructor
public class Rutina {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entrenador_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer","handler","usuario","especialidades","servicios","certificaciones","idiomas"})
    private Entrenador entrenador;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer","handler","password","planExpira","fechaRegistro"})
    private Usuario cliente;

    private String titulo;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    // JSON: [{"nombre":"Sentadilla","series":4,"reps":"10-12","descanso":"60s","notas":"Peso moderado"}]
    @Column(columnDefinition = "TEXT")
    private String ejerciciosJson;

    private String diasSemana; // "Lunes, Miércoles, Viernes"
    private String nivel;      // Principiante / Intermedio / Avanzado
    private Integer duracionMinutos;

    private Boolean activa = true;
    private Boolean esPlantilla = false;

    private LocalDateTime creadaEn = LocalDateTime.now();
}
