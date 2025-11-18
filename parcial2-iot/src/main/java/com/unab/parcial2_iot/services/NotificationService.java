package com.unab.parcial2_iot.services;

import com.unab.parcial2_iot.dto.AlertaOut;

public interface NotificationService {
    void notifyAlert(AlertaOut alerta);
}

