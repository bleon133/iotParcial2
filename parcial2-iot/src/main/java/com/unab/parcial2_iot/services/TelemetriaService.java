package com.unab.parcial2_iot.services;

import com.unab.parcial2_iot.dto.TelemetriaIn;

public interface TelemetriaService {
    void registrarLectura(TelemetriaIn in);
}