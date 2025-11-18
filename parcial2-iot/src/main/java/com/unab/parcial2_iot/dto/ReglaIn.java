package com.unab.parcial2_iot.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class ReglaIn {

    @NotNull
    private UUID variableId;    // <-- nombre correcto

    private UUID dispositivoId; // opcional: limitar a un dispositivo específico

    @NotBlank
    private String nombre;

    private String expresion;

    private String severidad;

    private Boolean habilitada;

    private Integer ventanaSegundos;

    // Nuevo: tipo de regla (expr|bands) y parámetros simples para bandas
    private String tipo; // "expr" (default) o "bands"
    private Double bandsHighValue; // si valor > bandsHighValue => severidadHigh
    private String bandsHighSeverity; // p.ej., "grave"
    private Double bandsLowValue;  // si valor < bandsLowValue => severidadLow
    private String bandsLowSeverity; // p.ej., "bajo"
    private Double bandsNormalMin; // rango normal opcional
    private Double bandsNormalMax;
}
