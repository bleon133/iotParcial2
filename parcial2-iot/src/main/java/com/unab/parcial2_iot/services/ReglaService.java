package com.unab.parcial2_iot.services;

import com.unab.parcial2_iot.dto.ReglaIn;
import com.unab.parcial2_iot.models.Regla;

import java.util.List;
import java.util.UUID;

public interface ReglaService {
    Regla crear(ReglaIn in);
    Regla actualizar(UUID id, ReglaIn in);
    void eliminar(UUID id);
    List<Regla> listarTodas();
    List<Regla> listarHabilitadas();

    void evaluarReglas(); // llamado por @Scheduled

    // Evaluación inmediata para un dispositivo/variable (para disparar alertas en ingesta)
    void evaluarPara(java.util.UUID dispositivoId, java.util.UUID variableId);
}
