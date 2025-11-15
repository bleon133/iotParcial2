package com.unab.parcial2_iot.services.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.unab.parcial2_iot.dto.AlertaOut;
import com.unab.parcial2_iot.models.Alerta;
import com.unab.parcial2_iot.models.Dispositivo;
import com.unab.parcial2_iot.models.Regla;
import com.unab.parcial2_iot.repositories.AlertaRepository;
import com.unab.parcial2_iot.services.AlertaService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AlertaServiceImpl implements AlertaService {

    private final AlertaRepository alertaRepo;
    private final ObjectMapper objectMapper;
    private final com.unab.parcial2_iot.services.SseHub sseHub;

    @Override
    public Alerta crear(Dispositivo d, Regla r, Map<String, Object> detalles) {
        // El builder de Alerta espera Map<String,Object> en 'detalles'
        Alerta a = Alerta.builder()
                .dispositivo(d)
                .regla(r)
                .ts(OffsetDateTime.now())
                .detalles(detalles) // <-- Map, NO String
                .build();
        a = alertaRepo.save(a);
        try {
            com.unab.parcial2_iot.dto.AlertaOut out = new com.unab.parcial2_iot.dto.AlertaOut(
                    a.getId(),
                    d.getId(), d.getNombre(),
                    r.getId(), r.getNombre(),
                    a.getTs(),
                    safeJson(a.getDetalles())
            );
            sseHub.broadcastAlerta(out);
        } catch (Exception ignored) {}
        return a;
    }

    @Override
    public Page<AlertaOut> listar(String filtroTipo, UUID id, String severidad, Pageable pageable) {
        Page<Alerta> page;
        boolean hasSev = severidad != null && !severidad.isBlank();
        if ("regla".equalsIgnoreCase(filtroTipo) && id != null) {
            page = hasSev
                    ? alertaRepo.findByRegla_IdAndRegla_SeveridadOrderByTsDesc(id, severidad, pageable)
                    : alertaRepo.findByRegla_IdOrderByTsDesc(id, pageable);
        } else if ("dispositivo".equalsIgnoreCase(filtroTipo) && id != null) {
            page = hasSev
                    ? alertaRepo.findByDispositivo_IdAndRegla_SeveridadOrderByTsDesc(id, severidad, pageable)
                    : alertaRepo.findByDispositivo_IdOrderByTsDesc(id, pageable);
        } else {
            page = hasSev
                    ? alertaRepo.findByRegla_SeveridadOrderByTsDesc(severidad, pageable)
                    : alertaRepo.findAllByOrderByTsDesc(pageable);
        }

        return page.map(a -> new AlertaOut(
                a.getId(),
                a.getDispositivo().getId(),
                a.getDispositivo().getNombre(),
                a.getRegla().getId(),
                a.getRegla().getNombre(),
                a.getTs(),
                safeJson(a.getDetalles()) // <-- pasamos String JSON al DTO
        ));
    }

    private String safeJson(Map<String, Object> detalles) {
        try {
            return objectMapper.writeValueAsString(detalles);
        } catch (Exception e) {
            return "{}";
        }
    }
}
