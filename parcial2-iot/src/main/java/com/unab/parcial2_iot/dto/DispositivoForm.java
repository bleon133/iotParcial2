package com.unab.parcial2_iot.dto;

import com.unab.parcial2_iot.models.Protocolo;
import com.unab.parcial2_iot.models.TipoDispositivo;
import lombok.Data;

import java.util.UUID;

/** DTO para el formulario Thymeleaf (incluye etiquetasJson como String). */
@Data
public class DispositivoForm {
    private UUID plantillaId;
    private String idExterno;
    private String nombre;
    private TipoDispositivo tipo;
    private Protocolo protocolo;       // opcional; si no viene, se usa el de la plantilla
    private String estado;             // por defecto "habilitado"
    private String topicoMqttTelemetria;
    private String topicoMqttComando;
    private Double latitud;
    private Double longitud;
    private String etiquetasJson;      // JSON opcional: {"ubicacion":"sala","modelo":"ESP32"}
}