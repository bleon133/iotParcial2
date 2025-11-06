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

    @NotBlank
    private String nombre;

    @NotBlank
    private String expresion;

    private String severidad;

    private Boolean habilitada;

    private Integer ventanaSegundos;
}