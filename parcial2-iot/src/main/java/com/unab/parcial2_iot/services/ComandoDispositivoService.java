package com.unab.parcial2_iot.services;


import com.unab.parcial2_iot.models.ComandoDispositivo;
import com.unab.parcial2_iot.models.EstadoComando;

import java.util.List;
import java.util.UUID;

public interface ComandoDispositivoService {

    ComandoDispositivo crear(UUID dispositivoId,
                             String comando,
                             String datosJson,
                             String solicitadoPor);

    List<ComandoDispositivo> historialPorDispositivo(UUID dispositivoId);

    ComandoDispositivo cambiarEstado(UUID comandoId,
                                     EstadoComando nuevoEstado,
                                     String mensajeError);
}