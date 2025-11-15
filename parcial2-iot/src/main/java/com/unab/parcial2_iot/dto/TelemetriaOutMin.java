package com.unab.parcial2_iot.dto;

import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TelemetriaOutMin {
    private UUID dispositivoId;
    private String dispositivoNombre;
    private UUID variableId;
    private String variableNombre;
    private OffsetDateTime ts;
    private Double numero;
    private Boolean booleano;
    private String texto;
    private String json; // stringificado
}
