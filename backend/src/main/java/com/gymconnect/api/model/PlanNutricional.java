package com.gymconnect.api.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entrenador_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer","handler","usuario","especialidades","servicios","certificaciones","idiomas"})
    private Entrenador entrenador;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer","handler","password","planExpira","fechaRegistro"})
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
