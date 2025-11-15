package com.unab.parcial2_iot.dto;

import com.unab.parcial2_iot.models.EstadoComando;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ComandoUpdateOut {
    private UUID id;
    private UUID dispositivoId;
    private String dispositivoNombre;
    private EstadoComando estado;
    private OffsetDateTime confirmadoEn;
    private String mensajeError;
    // opcionales (para insertar filas nuevas en UI cuando llegue ENVIADO)
    private OffsetDateTime solicitadoEn;
    private String comando;
    private String datos;
}
