package com.gymconnect.api.service;

import com.gymconnect.api.dto.AuthResponse;
import com.gymconnect.api.dto.LoginRequest;
import com.gymconnect.api.dto.RegistroRequest;
import com.gymconnect.api.model.Usuario;
import com.gymconnect.api.repository.UsuarioRepository;
import com.gymconnect.api.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepo;
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

        usuarioRepo.save(u);

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
