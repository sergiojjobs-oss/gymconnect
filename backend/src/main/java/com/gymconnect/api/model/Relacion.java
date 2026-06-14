package com.gymconnect.api.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

// Relación activa entre un cliente y un entrenador (tras el pago)
@Entity
@Table(name = "relaciones")
@Data
@NoArgsConstructor
public class Relacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "cliente_id", nullable = false)
    private Usuario cliente;

    @ManyToOne
    @JoinColumn(name = "entrenador_id", nullable = false)
    private Entrenador entrenador;

    @Enumerated(EnumType.STRING)
    private Estado estado = Estado.ACTIVA;

    private LocalDateTime fechaInicio = LocalDateTime.now();
    private LocalDateTime fechaFin;

    @Column(columnDefinition = "TEXT")
    private String notasPrivadas;

    public enum Estado {
        ACTIVA, CANCELADA, PENDIENTE_PAGO
    }
}
