package com.gymconnect.api.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "progreso_cliente")
@Data
@NoArgsConstructor
public class ProgresoCliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer","handler"})
    private Usuario cliente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entrenador_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer","handler","usuario","especialidades","servicios","certificaciones","idiomas"})
    private Entrenador entrenador;

    private LocalDate fecha;
    private Double peso;           // kg
    private Double grasaCorporal;  // %
    private Double musculatura;    // kg (opcional)

    @Column(columnDefinition = "TEXT")
    private String notas;          // Notas del cliente sobre la semana

    @Column(columnDefinition = "TEXT")
    private String notasEntrenador; // Feedback del entrenador

    @Column(columnDefinition = "TEXT")
    private String fotoUrl;        // foto progreso (ImgBB)

    private Integer energia;       // 1-5: cómo se ha sentido esta semana
    private Integer cumplimiento;  // % 0-100: cuánto ha cumplido la rutina

    private LocalDateTime creadoEn = LocalDateTime.now();
}
