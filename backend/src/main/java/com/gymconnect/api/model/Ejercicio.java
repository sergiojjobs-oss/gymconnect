package com.gymconnect.api.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "ejercicios")
@Data
@NoArgsConstructor
public class Ejercicio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;
    private String grupoMuscular;   // Pecho / Espalda / Piernas / Hombros / Brazos / Core / Cardio / Fullbody
    private String equipamiento;    // Ninguno / Mancuernas / Barra / Máquina / Bandas / TRX / Kettlebell

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    private String nivel;           // Principiante / Intermedio / Avanzado
    private String imagenUrl;

    // null = ejercicio público del sistema, not null = creado por un entrenador
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entrenador_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer","handler","usuario","especialidades","servicios","certificaciones","idiomas"})
    private Entrenador entrenador;

    private LocalDateTime creadoEn = LocalDateTime.now();
}
