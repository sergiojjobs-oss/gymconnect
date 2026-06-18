package com.gymconnect.api.service;

import com.gymconnect.api.model.Entrenador;
import com.gymconnect.api.model.Relacion;
import com.gymconnect.api.model.Usuario;
import com.gymconnect.api.repository.EntrenadorRepository;
import com.gymconnect.api.repository.RelacionRepository;
import com.gymconnect.api.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class SuscripcionService {

    private final UsuarioRepository usuarioRepo;
    private final RelacionRepository relacionRepo;
    private final EntrenadorRepository entrenadorRepo;

    @Value("${plan.free.max-clients}")
    private int freeMaxClientes;

    public boolean puedeAceptarCliente(Long entrenadorId) {
        Entrenador entrenador = entrenadorRepo.findById(entrenadorId)
                .orElseThrow(() -> new IllegalArgumentException("Entrenador no encontrado"));

        Usuario u = entrenador.getUsuario();
        long clientesActivos = relacionRepo.countByEntrenadorIdAndEstado(entrenadorId, Relacion.Estado.ACTIVA);

        return switch (u.getPlan()) {
            case FREE -> clientesActivos < freeMaxClientes;
            case PRO, ELITE -> true;
            default -> false;
        };
    }

    public void actualizarPlan(Long usuarioId, Usuario.PlanSuscripcion nuevoPlan) {
        Usuario u = usuarioRepo.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        u.setPlan(nuevoPlan);
        u.setPlanExpira(LocalDateTime.now().plusMonths(1));
        usuarioRepo.save(u);
    }

    public void cancelarPlan(Long usuarioId) {
        Usuario u = usuarioRepo.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        u.setPlan(Usuario.PlanSuscripcion.FREE);
        u.setPlanExpira(null);
        usuarioRepo.save(u);
    }
}
