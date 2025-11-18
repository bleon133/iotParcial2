package com.unab.parcial2_iot.models;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "device_credential", schema = "iot")
public class DeviceCredential {
    @Id
    @GeneratedValue
    @org.hibernate.annotations.UuidGenerator
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "dispositivo_id", nullable = false, unique = true)
    private Dispositivo dispositivo;

    @Column(name = "tipo", nullable = false)
    private String tipo; // 'symmetric' | 'x509'

    @Column(name = "clave_simetrica")
    private String claveSimetrica;

    @Column(name = "huella_x509")
    private String huellaX509;

    @Column(name = "activo", nullable = false)
    private boolean activo = true;

    @Column(name = "creado_en", nullable = false)
    private OffsetDateTime creadoEn;
}

