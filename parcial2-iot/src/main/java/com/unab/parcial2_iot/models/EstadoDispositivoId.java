package com.unab.parcial2_iot.models;

import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;
import java.util.UUID;

/** PK compuesta: (dispositivo_id, variable_plantilla_id) */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @EqualsAndHashCode
@Embeddable
public class EstadoDispositivoId implements Serializable {
    private UUID dispositivoId;
    private UUID variablePlantillaId;
}