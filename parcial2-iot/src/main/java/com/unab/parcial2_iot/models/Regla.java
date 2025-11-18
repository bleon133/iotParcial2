package com.unab.parcial2_iot.models;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.Map;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Entity
@Table(name = "regla", schema = "iot")
public class Regla {

    @Id
    @GeneratedValue
    @org.hibernate.annotations.UuidGenerator
    private java.util.UUID id;

    /** Variable objetivo de la regla */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "variable_plantilla_id", nullable = false)
    private VariablePlantilla variable;

    /** Opcional: limitar la regla a un dispositivo específico */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dispositivo_id")
    private Dispositivo dispositivo;

    @Column(nullable = false)
    private String nombre;

    /** Expresión evaluable (modo 'expr'), p.ej. "avg_5m > 50" */
    @Column(nullable = false)
    private String expresion;

    /** Severidad 'base' (info/warn/crit...). En modo 'bands' se calcula y va en detalles */
    private String severidad = "info";

    @Column(nullable = false)
    private boolean habilitada = true;

    @Column(name = "ventana_segundos")
    private Integer ventanaSegundos = 300;

    @Column(name = "creada_en", nullable = false)
    private OffsetDateTime creadaEn;

    /** Tipo de regla: 'expr' (por defecto) o 'bands' */
    @Column(name = "tipo", nullable = false)
    private String tipo = "expr";

    /** Config JSON para modo 'bands' u otros */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "config", nullable = false)
    private Map<String, Object> config;
}

