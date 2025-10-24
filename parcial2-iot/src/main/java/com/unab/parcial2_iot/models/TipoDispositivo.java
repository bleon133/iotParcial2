package com.unab.parcial2_iot.models;

/**
 * Enum PostgreSQL iot.tipo_dispositivo
 * Debe coincidir EXACTAMENTE con las etiquetas definidas en la BD.
 */
public enum TipoDispositivo {
    real,           // físico
    gemelo_digital, // twin
    api,
    conjunto_datos
}