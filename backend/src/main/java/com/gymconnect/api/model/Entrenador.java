package com.gymconnect.api.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "entrenadores")
@Data
@NoArgsConstructor
public class Entrenador {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    private String ciudad;
    private String bio;
    private Double precioMensual;
    private Double rating = 5.0;
    private Integer totalResenas = 0;
    private Boolean verificado = false;

    @ElementCollection
    @CollectionTable(name = "entrenador_especialidades", joinColumns = @JoinColumn(name = "entrenador_id"))
    @Column(name = "especialidad")
    private List<String> especialidades;

    @ElementCollection
    @CollectionTable(name = "entrenador_servicios", joinColumns = @JoinColumn(name = "entrenador_id"))
    @Column(name = "servicio")
    private List<String> servicios;

    private Integer aniosExperiencia;
    private String avatarColor;
}
