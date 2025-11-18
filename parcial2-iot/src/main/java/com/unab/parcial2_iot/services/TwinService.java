package com.unab.parcial2_iot.services;

import com.unab.parcial2_iot.dto.TwinOut;

import java.util.Map;
import java.util.UUID;

public interface TwinService {
    TwinOut getTwin(UUID dispositivoId);
    TwinOut patchDesired(UUID dispositivoId, Map<String, Object> desiredPatch);
    TwinOut updateReported(UUID dispositivoId, Map<String, Object> reportedPatch);
}

