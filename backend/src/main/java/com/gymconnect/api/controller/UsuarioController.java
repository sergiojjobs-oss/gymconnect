package com.gymconnect.api.controller;

import com.gymconnect.api.model.Usuario;
import com.gymconnect.api.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/usuario")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioRepository usuarioRepo;

    @GetMapping("/perfil")
    public ResponseEntity<?> perfil(@AuthenticationPrincipal UserDetails ud) {
        Usuario u = usuarioRepo.findByEmail(ud.getUsername())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        return ResponseEntity.ok(Map.of(
                "id", u.getId(),
                "nombre", u.getNombre(),
                "apellido", u.getApellido(),
                "email", u.getEmail(),
                "rol", u.getRol().name(),
                "plan", u.getPlan().name()
        ));
    }
}
