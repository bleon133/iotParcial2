package com.unab.parcial2_iot.models;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "estado_dispositivo", schema = "iot")
public class EstadoDispositivo {

    @EmbeddedId
    private EstadoDispositivoId id;

    @MapsId("dispositivoId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "dispositivo_id", nullable = false)
    private Dispositivo dispositivo;

    @MapsId("variablePlantillaId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "variable_plantilla_id", nullable = false)
    private VariablePlantilla variable;

    @Column(name = "ultimo_ts", nullable = false)
    private OffsetDateTime ultimoTs;

    @Column(name = "ultimo_numero")
    private Double ultimoNumero;

    @Column(name = "ultimo_booleano")
    private Boolean ultimoBooleano;

    @Column(name = "ultimo_texto")
    private String ultimoTexto;

    @Column(name = "ultimo_json", columnDefinition = "jsonb")
    private String ultimoJson;
}