package com.unab.parcial2_iot.models;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;

import java.time.OffsetDateTime;
import java.util.Map;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Entity
@Table(name = "comando_dispositivo", schema = "iot",
        indexes = @Index(name = "idx_cmd_dev_ts", columnList = "dispositivo_id, solicitado_en DESC"))
public class ComandoDispositivo {

    @Id
    @GeneratedValue
    @org.hibernate.annotations.UuidGenerator
    private java.util.UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "dispositivo_id", nullable = false)
    private Dispositivo dispositivo;

    /** Acción textual (ej. 'set_umbral','encender') */
    @Column(nullable = false)
    private String comando;

    /** Payload JSONB opcional */
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> datos;

    /** Estado del comando (enum PostgreSQL) */
    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    @Column(name = "estado", nullable = false, columnDefinition = "iot.estado_comando")
    private EstadoComando estado = EstadoComando.ENVIADO;

    /** Auditoría */
    @Column(name = "solicitado_por")
    private String solicitadoPor;

    @Column(name = "solicitado_en", nullable = false)
    private OffsetDateTime solicitadoEn;

    @Column(name = "confirmado_en")
    private OffsetDateTime confirmadoEn;

    @Column(name = "mensaje_error")
    private String mensajeError;
}