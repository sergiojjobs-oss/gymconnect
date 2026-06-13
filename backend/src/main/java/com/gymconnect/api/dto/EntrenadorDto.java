package com.gymconnect.api.dto;

import com.gymconnect.api.model.Entrenador;
import lombok.Data;

import java.util.List;

@Data
public class EntrenadorDto {
    private Long id;
    private String nombre;
    private String apellido;
    private String ciudad;
    private String bio;
    private Double precioMensual;
    private Double rating;
    private Integer totalResenas;
    private Boolean verificado;
    private Boolean pro;
    private List<String> especialidades;
    private List<String> servicios;
    private Integer aniosExperiencia;
    private String avatarColor;

    public static EntrenadorDto from(Entrenador e) {
        EntrenadorDto dto = new EntrenadorDto();
        dto.setId(e.getId());
        dto.setNombre(e.getUsuario().getNombre());
        dto.setApellido(e.getUsuario().getApellido());
        dto.setCiudad(e.getCiudad());
        dto.setBio(e.getBio());
        dto.setPrecioMensual(e.getPrecioMensual());
        dto.setRating(e.getRating());
        dto.setTotalResenas(e.getTotalResenas());
        dto.setVerificado(e.getVerificado());
        dto.setPro(e.getUsuario().getPlan() != com.gymconnect.api.model.Usuario.PlanSuscripcion.FREE);
        dto.setEspecialidades(e.getEspecialidades());
        dto.setServicios(e.getServicios());
        dto.setAniosExperiencia(e.getAniosExperiencia());
        dto.setAvatarColor(e.getAvatarColor());
        return dto;
    }
}
