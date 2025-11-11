package com.unab.parcial2_iot.models;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode
@Embeddable
public class EstadoDispositivoId implements Serializable {

    @Column(name = "dispositivo_id")
    private UUID dispositivoId;

    @Column(name = "variable_plantilla_id")
    private UUID variablePlantillaId;
}