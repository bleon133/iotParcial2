package com.unab.parcial2_iot.models;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.OffsetDateTime;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Entity
@Table(name = "plantilla", schema = "iot")
public class Plantilla {

    /** UUID generado por Hibernate (equivalente a gen_random_uuid() en DB) */
    @Id
    @GeneratedValue
    @UuidGenerator
    private java.util.UUID id;

    /** Nombre humano único de la plantilla/modelo */
    @Column(nullable = false, unique = true)
    private String nombre;

    /** Descripción libre */
    private String descripcion;

    /** Protocolo por defecto del modelo (enum PostgreSQL) */
    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    @Column(name = "protocolo_predeterminado", nullable = false, columnDefinition = "iot.protocolo")
    private Protocolo protocoloPredeterminado = Protocolo.mqtt;

    /** Auditoría: creado_en (TIMESTAMPTZ) */
    @Column(name = "creado_en", nullable = false)
    private OffsetDateTime creadoEn;
}