package com.unab.parcial2_iot.models;

import jakarta.persistence.*;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Entity
@Table(name = "variable_plantilla", schema = "iot",
        uniqueConstraints = @UniqueConstraint(columnNames = {"plantilla_id", "nombre"}))
public class VariablePlantilla {

    @Id
    @GeneratedValue
    @org.hibernate.annotations.UuidGenerator
    private java.util.UUID id;

    /** FK a Plantilla */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "plantilla_id", nullable = false)
    private Plantilla plantilla;

    /** Clave técnica (payload MQTT) */
    @Column(nullable = false)
    private String nombre;

    /** Etiqueta visible en UI */
    private String etiqueta;

    /** Tipo de dato (enum PostgreSQL) */
    @Enumerated(EnumType.STRING)
    @org.hibernate.annotations.JdbcType(org.hibernate.dialect.PostgreSQLEnumJdbcType.class)
    @Column(name = "tipo_dato", nullable = false, columnDefinition = "iot.tipo_dato")
    private TipoDato tipoDato;

    /** Unidad de medida (°C, %, etc.) */
    private String unidad;

    /** ¿Admite escritura desde la nube? */
    @Column(nullable = false)
    private boolean escribible = false;

    /** Límites sugeridos / validaciones UI */
    private java.math.BigDecimal minimo;
    private java.math.BigDecimal maximo;

    /** Decimales sugeridos para mostrar */
    @Column(name = "precision")
    private Integer precisionDecimales;

    /** Periodicidad esperada de lecturas (ms) */
    @Column(name = "muestreo_ms")
    private Integer muestreoMs;
}