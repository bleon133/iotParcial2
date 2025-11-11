package com.unab.parcial2_iot.services;

import com.unab.parcial2_iot.dto.EstadoDispositivoOut;

import java.util.List;
import java.util.UUID;

public interface EstadoDispositivoService {

    /**
     * Estado actual de todas las variables de todos los dispositivos.
     */
    List<EstadoDispositivoOut> listarTodos();

    /**
     * Estado actual de un solo dispositivo (todas sus variables).
     */
    List<EstadoDispositivoOut> listarPorDispositivo(UUID dispositivoId);
}