package com.gymconnect.api.dto;

import com.gymconnect.api.model.Usuario;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private String tipo = "Bearer";
    private Long id;
    private String nombre;
    private String email;
    private Usuario.Rol rol;
    private Usuario.PlanSuscripcion plan;

    public AuthResponse(String token, Long id, String nombre, String email,
                        Usuario.Rol rol, Usuario.PlanSuscripcion plan) {
        this.token = token;
        this.id = id;
        this.nombre = nombre;
        this.email = email;
        this.rol = rol;
        this.plan = plan;
    }
}
