package com.unab.parcial2_iot.models;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Entity
@Table(name = "panel", schema = "iot")
public class Panel {
    @Id @GeneratedValue @org.hibernate.annotations.UuidGenerator
    private UUID id;
    @Column(nullable = false)
    private String nombre;
    private String descripcion;
    @Column(name = "creado_en", nullable = false)
    private OffsetDateTime creadoEn;
}

