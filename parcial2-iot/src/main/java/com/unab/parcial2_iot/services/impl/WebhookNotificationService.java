package com.unab.parcial2_iot.services.impl;

import com.unab.parcial2_iot.config.NotifyProps;
import com.unab.parcial2_iot.dto.AlertaOut;
import com.unab.parcial2_iot.services.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
@RequiredArgsConstructor
@Slf4j
public class WebhookNotificationService implements NotificationService {

    private final NotifyProps props;
    private final RestClient rest = RestClient.create();

    @Override
    public void notifyAlert(AlertaOut alerta) {
        if (!props.isEnabled() || props.getWebhookUrl() == null || props.getWebhookUrl().isBlank() || alerta == null) return;
        try {
            rest.post().uri(props.getWebhookUrl())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(alerta)
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            log.warn("Notify webhook failed: {}", e.toString());
        }
    }
}

