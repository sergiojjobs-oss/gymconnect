package com.gymconnect.api.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;

@Entity
@Table(name = "sesiones")
@Data
@NoArgsConstructor
public class Sesion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entrenador_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer","handler","usuario","especialidades","servicios","certificaciones","idiomas"})
    private Entrenador entrenador;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer","handler","password","planExpira","fechaRegistro"})
    private Usuario cliente;

    private LocalDate fecha;
    private LocalTime hora;
    private Integer duracionMinutos = 60;

    private String tipo;    // Presencial / Online / Seguimiento

    @Column(columnDefinition = "TEXT")
    private String notas;

    @Enumerated(EnumType.STRING)
    private Estado estado = Estado.PROGRAMADA;

    private LocalDateTime creadaEn = LocalDateTime.now();

    public enum Estado {
        PROGRAMADA, COMPLETADA, CANCELADA
    }
}
