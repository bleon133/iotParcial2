package com.unab.parcial2_iot.services;


import com.unab.parcial2_iot.models.Dispositivo;

import java.util.List;
import java.util.UUID;

public interface DispositivoService {
    List<Dispositivo> listar();
    Dispositivo crear(Dispositivo nuevo);
    void eliminar(UUID id);
}