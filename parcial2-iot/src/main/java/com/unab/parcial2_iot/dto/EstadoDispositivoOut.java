package com.unab.parcial2_iot.dto;

import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EstadoDispositivoOut {

    private UUID dispositivoId;
    private String dispositivoNombre;

    private UUID variableId;
    private String variableNombre;
    private String variableEtiqueta;

    private OffsetDateTime ultimoTs;

    private Double ultimoNumero;
    private Boolean ultimoBooleano;
    private String ultimoTexto;
    private String ultimoJson;

    private Boolean tieneRegla;
}
