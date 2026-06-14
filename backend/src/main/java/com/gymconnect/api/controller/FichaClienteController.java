package com.gymconnect.api.controller;

import com.gymconnect.api.model.Entrenador;
import com.gymconnect.api.model.FichaCliente;
import com.gymconnect.api.model.Usuario;
import com.gymconnect.api.repository.EntrenadorRepository;
import com.gymconnect.api.repository.FichaClienteRepository;
import com.gymconnect.api.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/ficha-cliente")
@RequiredArgsConstructor
public class FichaClienteController {

    private final FichaClienteRepository fichaRepo;
    private final EntrenadorRepository entrenadorRepo;
    private final UsuarioRepository usuarioRepo;

    @GetMapping("/{clienteId}")
    public ResponseEntity<?> getFicha(@AuthenticationPrincipal UserDetails ud,
                                       @PathVariable Long clienteId) {
        Entrenador ent = entrenadorRepo.findByUsuarioEmail(ud.getUsername())
                .orElseThrow(() -> new RuntimeException("No encontrado"));
        return fichaRepo.findByClienteIdAndEntrenadorId(clienteId, ent.getId())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }

    @SuppressWarnings("unchecked")
    @PostMapping("/{clienteId}")
    public ResponseEntity<?> saveFicha(@AuthenticationPrincipal UserDetails ud,
                                        @PathVariable Long clienteId,
                                        @RequestBody Map<String, Object> body) {
        Entrenador ent = entrenadorRepo.findByUsuarioEmail(ud.getUsername())
                .orElseThrow(() -> new RuntimeException("No encontrado"));
        Usuario cliente = usuarioRepo.findById(clienteId)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));

        FichaCliente ficha = fichaRepo.findByClienteIdAndEntrenadorId(clienteId, ent.getId())
                .orElse(new FichaCliente());

        ficha.setCliente(cliente);
        ficha.setEntrenador(ent);

        if (body.get("edad") != null) ficha.setEdad(((Number) body.get("edad")).intValue());
        if (body.get("sexo") != null) ficha.setSexo((String) body.get("sexo"));
        if (body.get("alturacm") != null) ficha.setAlturacm(((Number) body.get("alturacm")).doubleValue());
        if (body.get("pesoObjetivo") != null) ficha.setPesoObjetivo(((Number) body.get("pesoObjetivo")).doubleValue());
        if (body.get("objetivo") != null) ficha.setObjetivo((String) body.get("objetivo"));
        if (body.get("nivelFitness") != null) ficha.setNivelFitness((String) body.get("nivelFitness"));
        if (body.get("lesiones") != null) ficha.setLesiones((String) body.get("lesiones"));
        if (body.get("condicionesMedicas") != null) ficha.setCondicionesMedicas((String) body.get("condicionesMedicas"));
        if (body.get("cinturaInicial") != null) ficha.setCinturaInicial(((Number) body.get("cinturaInicial")).doubleValue());
        if (body.get("caderaInicial") != null) ficha.setCaderaInicial(((Number) body.get("caderaInicial")).doubleValue());
        if (body.get("pechoInicial") != null) ficha.setPechoInicial(((Number) body.get("pechoInicial")).doubleValue());
        if (body.get("horasSuenio") != null) ficha.setHorasSuenio(((Number) body.get("horasSuenio")).intValue());
        if (body.get("diasDisponibles") != null) ficha.setDiasDisponibles(((Number) body.get("diasDisponibles")).intValue());
        if (body.get("equipamientoDisponible") != null) ficha.setEquipamientoDisponible((String) body.get("equipamientoDisponible"));
        if (body.get("motivacion") != null) ficha.setMotivacion((String) body.get("motivacion"));
        if (body.get("otrasNotas") != null) ficha.setOtrasNotas((String) body.get("otrasNotas"));

        ficha.setActualizadaEn(LocalDateTime.now());
        return ResponseEntity.ok(fichaRepo.save(ficha));
    }
}
