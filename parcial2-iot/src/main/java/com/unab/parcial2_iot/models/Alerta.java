package com.unab.parcial2_iot.models;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.Map;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Entity
@Table(name = "alerta", schema = "iot",
        indexes = @Index(name = "idx_alerta_dev_ts", columnList = "dispositivo_id, ts DESC"))
public class Alerta {

    @Id
    @GeneratedValue
    @org.hibernate.annotations.UuidGenerator
    private java.util.UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "dispositivo_id", nullable = false)
    private Dispositivo dispositivo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "regla_id", nullable = false)
    private Regla regla;

    @Column(name = "ts", nullable = false)
    private OffsetDateTime ts;

    /** Detalles del disparo (valor observado, umbral, etc.) */
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> detalles;
}