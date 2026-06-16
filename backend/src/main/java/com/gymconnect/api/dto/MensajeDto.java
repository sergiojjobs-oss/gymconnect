package com.gymconnect.api.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MensajeDto {
    private Long id;
    private Long remitenteId;
    private String remitenteNombre;
    private Long destinatarioId;
    private String contenido;
    private LocalDateTime fechaEnvio;
    private boolean leido;
    private boolean eliminado;
    private ReplyDto replyTo;

    @lombok.Data
    public static class ReplyDto {
        private String autor;
        private String texto;
    }
}
