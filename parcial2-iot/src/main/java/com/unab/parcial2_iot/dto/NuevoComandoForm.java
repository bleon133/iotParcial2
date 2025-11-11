package com.unab.parcial2_iot.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class NuevoComandoForm {
    private UUID dispositivoId;
    private String comando;
    private String datos;          // <- mismo nombre que th:field="*{datos}"
    private String solicitadoPor;
}
