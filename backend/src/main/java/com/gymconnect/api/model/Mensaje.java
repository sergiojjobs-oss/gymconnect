package com.gymconnect.api.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "mensajes")
@Data
@NoArgsConstructor
public class Mensaje {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long remitenteId;

    @Column(nullable = false)
    private Long destinatarioId;

    @Column(nullable = false, length = 2000)
    private String contenido;

    @Column(nullable = false)
    private LocalDateTime fechaEnvio = LocalDateTime.now();

    private boolean leido = false;
    private boolean eliminado = false;
}
