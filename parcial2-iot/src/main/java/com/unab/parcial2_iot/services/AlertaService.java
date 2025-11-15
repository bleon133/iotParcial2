package com.unab.parcial2_iot.services;

import com.unab.parcial2_iot.dto.AlertaOut;
import com.unab.parcial2_iot.models.Alerta;
import com.unab.parcial2_iot.models.Dispositivo;
import com.unab.parcial2_iot.models.Regla;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Map;

public interface AlertaService {
    Alerta crear(Dispositivo d, Regla r, Map<String, Object> detalles);
    Page<AlertaOut> listar(String filtroTipo, java.util.UUID id, String severidad, Pageable pageable);
}
