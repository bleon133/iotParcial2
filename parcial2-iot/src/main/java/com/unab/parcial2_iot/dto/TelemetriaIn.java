package com.unab.parcial2_iot.dto;


import lombok.Data;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Permite identificar el destino por:
 *  - dispositivoId (UUID) o idExterno (String)
 *  - variableId (UUID) o variableNombre (String) [nombre técnico de la variable]
 * Debe venir EXACTAMENTE un valor: numero | booleano | texto | json
 */
@Data
public class TelemetriaIn {
    // Identificación del dispositivo
    private UUID dispositivoId;
    private String idExterno;

    // Identificación de la variable (de la plantilla del dispositivo)
    private UUID variableId;
    private String variableNombre;

    // Timestamp opcional (si no, now())
    private OffsetDateTime ts;

    // Valores (uno solo)
    private Double numero;
    private Boolean booleano;
    private String texto;
    private Map<String, Object> json;

    // Etiquetas opcionales por lectura
    private Map<String, Object> etiquetas;
}