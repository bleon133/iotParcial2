package com.unab.parcial2_iot.models;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Entity
@Table(name = "routing_rule", schema = "iot")
public class RoutingRule {
    @Id
    @GeneratedValue
    @org.hibernate.annotations.UuidGenerator
    private UUID id;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private boolean enabled = true;

    @Column(nullable = false)
    private String sink; // 'webhook', 'file', etc.

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "match", nullable = false)
    private Map<String,Object> match;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "config", nullable = false)
    private Map<String,Object> config;

    @Column(name = "creado_en", nullable = false)
    private OffsetDateTime creadoEn;
}

