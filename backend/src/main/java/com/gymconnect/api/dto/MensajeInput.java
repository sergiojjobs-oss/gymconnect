package com.gymconnect.api.dto;

import lombok.Data;

@Data
public class MensajeInput {
    private Long destinatarioId;
    private String contenido;
    private Long replyToId;
}
