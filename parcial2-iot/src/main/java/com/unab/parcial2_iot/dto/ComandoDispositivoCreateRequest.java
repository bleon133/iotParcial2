package com.unab.parcial2_iot.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class ComandoDispositivoCreateRequest {

    private UUID dispositivoId;
    private String comando;
    /**
     * JSON en texto. Ej:
     * {"umbral": 50} o {"accion": "on"}
     */
    private String datosJson;
    private String solicitadoPor;
}