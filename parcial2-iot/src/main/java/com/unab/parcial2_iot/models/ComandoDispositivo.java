package com.unab.parcial2_iot.models;


import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcType;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "comando_dispositivo", schema = "iot")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ComandoDispositivo {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dispositivo_id", nullable = false)
    private Dispositivo dispositivo;

    @Column(nullable = false)
    private String comando;

    /**
     * Columna 'datos' es jsonb en PostgreSQL.
     * Usamos String: debe venir como JSON válido ({"umbral":50}, etc.).
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private String datos;

    /**
     * Columna 'estado' es enum PostgreSQL iot.estado_comando.
     * Lo mapeamos como Enum Java + tipo específico PG.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, columnDefinition = "iot.estado_comando")
    @JdbcType(PostgreSQLEnumJdbcType.class)
    private EstadoComando estado;

    @Column(name = "solicitado_por")
    private String solicitadoPor;

    @Column(name = "solicitado_en", nullable = false)
    private OffsetDateTime solicitadoEn;

    @Column(name = "confirmado_en")
    private OffsetDateTime confirmadoEn;

    @Column(name = "mensaje_error")
    private String mensajeError;
}