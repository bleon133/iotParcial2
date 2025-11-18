package com.unab.parcial2_iot.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TwinOut {
    private UUID dispositivoId;
    private String dispositivoNombre;
    private String idExterno;
    private Map<String, Object> desired;
    private Map<String, Object> reported;
    private Integer version;
    private OffsetDateTime updatedAt;
}

