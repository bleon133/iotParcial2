package com.unab.parcial2_iot.models;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

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

    @Column(nullable = false)
    private String nombre;

    /** Expresión evaluable por un job externo (p.ej., "avg_5m > 50") */
    @Column(nullable = false)
    private String expresion;

    /** Severidad libre (info/warn/crit...) */
    private String severidad = "info";

    @Column(nullable = false)
    private boolean habilitada = true;

    @Column(name = "ventana_segundos")
    private Integer ventanaSegundos = 300;

    @Column(name = "creada_en", nullable = false)
    private OffsetDateTime creadaEn;
}