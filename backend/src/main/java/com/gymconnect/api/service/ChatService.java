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

    public Mensaje guardar(Long remitenteId, Long destinatarioId, String contenido, Long replyToId) {
        Mensaje m = new Mensaje();
        m.setRemitenteId(remitenteId);
        m.setDestinatarioId(destinatarioId);
        m.setContenido(contenido);
        m.setReplyToId(replyToId);
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

    public void marcarLeidos(Long destinatarioId, Long remitenteId) {
        List<Mensaje> pendientes = mensajeRepo.findByRemitenteIdAndDestinatarioIdAndLeidoFalse(remitenteId, destinatarioId);
        pendientes.forEach(m -> m.setLeido(true));
        mensajeRepo.saveAll(pendientes);
    }

    public MensajeDto toDto(Mensaje m) {
        MensajeDto dto = new MensajeDto();
        dto.setId(m.getId());
        dto.setRemitenteId(m.getRemitenteId());
        dto.setDestinatarioId(m.getDestinatarioId());
        dto.setContenido(m.getContenido());
        dto.setFechaEnvio(m.getFechaEnvio());
        dto.setLeido(m.isLeido());
        dto.setEliminado(m.isEliminado());

        usuarioRepo.findById(m.getRemitenteId())
                .ifPresent(u -> dto.setRemitenteNombre(u.getNombre() + " " + u.getApellido()));

        if (m.getReplyToId() != null) {
            mensajeRepo.findById(m.getReplyToId()).ifPresent(orig -> {
                MensajeDto.ReplyDto r = new MensajeDto.ReplyDto();
                r.setTexto(orig.isEliminado() ? "Mensaje eliminado" : orig.getContenido());
                usuarioRepo.findById(orig.getRemitenteId())
                    .ifPresent(u -> r.setAutor(u.getNombre()));
                dto.setReplyTo(r);
            });
        }

        return dto;
    }
}
