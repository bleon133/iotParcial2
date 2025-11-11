package com.unab.parcial2_iot.dto;

import com.unab.parcial2_iot.models.EstadoComando;
import lombok.Builder;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
public class ComandoDispositivoOut {

    private UUID id;

    private UUID dispositivoId;
    private String dispositivoNombre;

    private String comando;
    private String datosJson;

    private EstadoComando estado;

    private String solicitadoPor;
    private OffsetDateTime solicitadoEn;
    private OffsetDateTime confirmadoEn;

    private String mensajeError;
}