package com.gymconnect.api.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "planes_nutricionales")
@Data
@NoArgsConstructor
public class PlanNutricional {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "entrenador_id", nullable = false)
    private Entrenador entrenador;

    @ManyToOne
    @JoinColumn(name = "cliente_id", nullable = false)
    private Usuario cliente;

    private String titulo;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    // JSON: [{"nombre":"Desayuno","hora":"08:00","alimentos":"Avena 80g, leche 200ml, plátano","calorias":400}]
    @Column(columnDefinition = "TEXT")
    private String comidasJson;

    private Integer caloriasTotal;
    private Integer proteinasG;
    private Integer carbohidratosG;
    private Integer grasasG;

    private LocalDateTime creadoEn = LocalDateTime.now();
    private LocalDateTime actualizadoEn = LocalDateTime.now();
}
