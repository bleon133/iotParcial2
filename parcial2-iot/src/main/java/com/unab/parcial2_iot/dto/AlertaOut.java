package com.unab.parcial2_iot.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data @AllArgsConstructor
public class AlertaOut {
    private UUID id;
    private UUID dispositivoId;
    private String dispositivoNombre;
    private UUID reglaId;
    private String reglaNombre;
    private OffsetDateTime ts;
    private String detallesJson; // tal cual para mostrar en <pre>
}
