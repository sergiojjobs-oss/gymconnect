package com.gymconnect.api.service;

import com.gymconnect.api.dto.MensajeDto;
import com.gymconnect.api.model.Mensaje;
import com.gymconnect.api.model.Usuario;
import com.gymconnect.api.repository.MensajeRepository;
import com.gymconnect.api.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final MensajeRepository mensajeRepo;
    private final UsuarioRepository usuarioRepo;

    public Mensaje guardar(Long remitenteId, Long destinatarioId, String contenido) {
        Mensaje m = new Mensaje();
        m.setRemitenteId(remitenteId);
        m.setDestinatarioId(destinatarioId);
        m.setContenido(contenido);
        return mensajeRepo.save(m);
    }

    public List<MensajeDto> getConversacion(Long usuarioA, Long usuarioB) {
        return mensajeRepo.findConversacion(usuarioA, usuarioB)
                .stream()
                .map(this::toDto)
                .toList();
    }

    public long noLeidos(Long usuarioId) {
        return mensajeRepo.countByDestinatarioIdAndLeidoFalse(usuarioId);
    }

    public MensajeDto toDto(Mensaje m) {
        MensajeDto dto = new MensajeDto();
        dto.setId(m.getId());
        dto.setRemitenteId(m.getRemitenteId());
        dto.setDestinatarioId(m.getDestinatarioId());
        dto.setContenido(m.getContenido());
        dto.setFechaEnvio(m.getFechaEnvio());
        dto.setLeido(m.isLeido());

        usuarioRepo.findById(m.getRemitenteId())
                .ifPresent(u -> dto.setRemitenteNombre(u.getNombre() + " " + u.getApellido()));

        return dto;
    }
}
