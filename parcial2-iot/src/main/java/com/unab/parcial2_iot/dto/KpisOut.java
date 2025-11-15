package com.unab.parcial2_iot.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KpisOut {
    private long totalDispositivos;
    private long enLinea;
    private long alertas24h;
    private long muestrasHoy;
}

