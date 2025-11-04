package com.unab.parcial2_iot.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class ReglaIn {
    @NotNull
    private UUID variablePlantillaId;
    @NotNull
    private String nombre;
    @NotNull
    private String expresion;       // "avg > 50", "avg_10m >= 60", "max < 10"
    private String severidad = "info";
    private Boolean habilitada = true;
    private Integer ventanaSegundos = 300; // override si no viene _Nm en expresion
}
