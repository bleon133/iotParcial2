package com.unab.parcial2_iot.services;

import com.unab.parcial2_iot.models.Telemetria;

public interface TelemetryRouter {
    void route(Telemetria telemetria);
}

