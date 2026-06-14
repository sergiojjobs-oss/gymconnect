package com.gymconnect.api.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "resenas", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"cliente_id", "entrenador_id"})
})
@Data
@NoArgsConstructor
public class Resena {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer","handler","password","planExpira","fechaRegistro"})
    private Usuario cliente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entrenador_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer","handler","usuario","especialidades","servicios","certificaciones","idiomas"})
    private Entrenador entrenador;

    private Integer estrellas; // 1-5
    private String comentario;
    private LocalDateTime creadaEn = LocalDateTime.now();
}
