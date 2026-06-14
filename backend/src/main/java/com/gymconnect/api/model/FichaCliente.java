package com.gymconnect.api.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "fichas_cliente")
@Data
@NoArgsConstructor
public class FichaCliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false, unique = true)
    @JsonIgnoreProperties({"hibernateLazyInitializer","handler","password","planExpira","fechaRegistro"})
    private Usuario cliente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entrenador_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer","handler","usuario","especialidades","servicios","certificaciones","idiomas"})
    private Entrenador entrenador;

    // Datos personales
    private Integer edad;
    private String sexo;            // M / F / Otro
    private Double alturacm;
    private Double pesoObjetivo;

    // Objetivo
    private String objetivo;        // Perder peso / Ganar músculo / Rendimiento / Salud general / Otro
    private String nivelFitness;    // Principiante / Intermedio / Avanzado

    // Historial médico
    @Column(columnDefinition = "TEXT")
    private String lesiones;        // Texto libre: lesiones o limitaciones físicas

    @Column(columnDefinition = "TEXT")
    private String condicionesMedicas;

    // Medidas iniciales
    private Double cinturaInicial;
    private Double caderaInicial;
    private Double pechoInicial;

    // Estilo de vida
    private Integer horasSuenio;        // horas de sueño habituales
    private Integer diasDisponibles;    // días/semana para entrenar
    private String equipamientoDisponible; // gym / casa / parque / nada

    @Column(columnDefinition = "TEXT")
    private String motivacion;      // Por qué quiere cambiar

    @Column(columnDefinition = "TEXT")
    private String otrasNotas;

    private LocalDateTime creadaEn = LocalDateTime.now();
    private LocalDateTime actualizadaEn = LocalDateTime.now();
}
