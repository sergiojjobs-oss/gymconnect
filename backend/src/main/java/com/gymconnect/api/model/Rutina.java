package com.gymconnect.api.model;

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

    @ManyToOne
    @JoinColumn(name = "entrenador_id", nullable = false)
    private Entrenador entrenador;

    // Si es null, es una rutina genérica del entrenador; si tiene valor, es para ese cliente
    @ManyToOne
    @JoinColumn(name = "cliente_id")
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

    private LocalDateTime creadaEn = LocalDateTime.now();
}
