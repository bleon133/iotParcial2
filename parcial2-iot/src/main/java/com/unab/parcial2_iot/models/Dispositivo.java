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
@Table(name = "dispositivo", schema = "iot")
public class Dispositivo {

    @Id
    @GeneratedValue
    @org.hibernate.annotations.UuidGenerator
    private java.util.UUID id;

    /** FK a Plantilla/modelo */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "plantilla_id", nullable = false)
    private Plantilla plantilla;

    /** DeviceID real/firmware (único) */
    @Column(name = "id_externo", nullable = false, unique = true)
    private String idExterno;

    /** Nombre humano */
    @Column(nullable = false)
    private String nombre;

    /** Tipo de dispositivo (enum PostgreSQL) */
    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    @Column(name = "tipo", nullable = false, columnDefinition = "iot.tipo_dispositivo")
    private TipoDispositivo tipo;

    /** Estado operativo (texto libre) */
    private String estado = "habilitado";

    /** Protocolo/transporte */
    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    @Column(name = "protocolo", nullable = false, columnDefinition = "iot.protocolo")
    private Protocolo protocolo = Protocolo.mqtt;

    /** Tópicos MQTT (opcionales) */
    @Column(name = "topico_mqtt_telemetria")
    private String topicoMqttTelemetria;

    @Column(name = "topico_mqtt_comando")
    private String topicoMqttComando;

    /** Ubicación (opcional) */
    private Double latitud;
    private Double longitud;

    /** Etiquetas/tags JSONB (ej. {"ubicacion":"sala","modelo":"ESP32"}) */
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> etiquetas;

    /** Auditoría */
    @Column(name = "creado_en", nullable = false)
    private OffsetDateTime creadoEn;
}