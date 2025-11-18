package com.unab.parcial2_iot.models;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "device_twin", schema = "iot")
public class DeviceTwin {

    @Id
    @Column(name = "dispositivo_id")
    private UUID dispositivoId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dispositivo_id", insertable = false, updatable = false)
    private Dispositivo dispositivo;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "desired", nullable = false)
    private Map<String, Object> desired;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "reported", nullable = false)
    private Map<String, Object> reported;

    @Column(name = "version", nullable = false)
    private Integer version;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
}

