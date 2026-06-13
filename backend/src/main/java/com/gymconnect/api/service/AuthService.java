package com.gymconnect.api.service;

import com.gymconnect.api.dto.AuthResponse;
import com.gymconnect.api.dto.LoginRequest;
import com.gymconnect.api.dto.RegistroRequest;
import com.gymconnect.api.model.Entrenador;
import com.gymconnect.api.model.Usuario;
import com.gymconnect.api.repository.EntrenadorRepository;
import com.gymconnect.api.repository.UsuarioRepository;
import com.gymconnect.api.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepo;
    private final EntrenadorRepository entrenadorRepo;
    private final PasswordEncoder encoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authManager;

    public AuthResponse registro(RegistroRequest req) {
        if (usuarioRepo.existsByEmail(req.getEmail())) {
            throw new IllegalArgumentException("El email ya está registrado");
        }

        Usuario u = new Usuario();
        u.setNombre(req.getNombre());
        u.setApellido(req.getApellido());
        u.setEmail(req.getEmail());
        u.setPassword(encoder.encode(req.getPassword()));
        u.setRol(req.getRol());
        u.setPlan(Usuario.PlanSuscripcion.FREE);

        u = usuarioRepo.save(u);

        if (u.getRol() == Usuario.Rol.ENTRENADOR) {
            Entrenador ent = new Entrenador();
            ent.setUsuario(u);
            ent.setRating(5.0);
            ent.setTotalResenas(0);
            ent.setVerificado(false);
            ent.setPrecioMensual(30.0);
            ent.setEspecialidades(List.of());
            ent.setServicios(List.of());
            entrenadorRepo.save(ent);
        }

        String token = jwtUtil.generarToken(u.getEmail());
        return new AuthResponse(token, u.getId(), u.getNombre(), u.getEmail(), u.getRol(), u.getPlan());
    }

    public AuthResponse login(LoginRequest req) {
        authManager.authenticate(new UsernamePasswordAuthenticationToken(req.getEmail(), req.getPassword()));

        Usuario u = usuarioRepo.findByEmail(req.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        String token = jwtUtil.generarToken(u.getEmail());
        return new AuthResponse(token, u.getId(), u.getNombre(), u.getEmail(), u.getRol(), u.getPlan());
    }
}
