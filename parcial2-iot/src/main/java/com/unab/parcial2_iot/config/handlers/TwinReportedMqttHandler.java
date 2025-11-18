package com.unab.parcial2_iot.config.handlers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.unab.parcial2_iot.repositories.DispositivoRepository;
import com.unab.parcial2_iot.services.TwinService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.integration.mqtt.support.MqttHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessagingException;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class TwinReportedMqttHandler implements MessageHandler {

    private final ObjectMapper objectMapper;
    private final DispositivoRepository dispositivoRepository;
    private final TwinService twinService;

    @Override
    public void handleMessage(Message<?> message) throws MessagingException {
        String topic = (String) message.getHeaders().get(MqttHeaders.RECEIVED_TOPIC);
        String payload = String.valueOf(message.getPayload());

        try {
            // Esperado: devices/{idExterno}/twin/reported
            String[] parts = topic.split("/");
            String idExterno = parts.length >= 2 ? parts[1] : null;
            if (idExterno == null || idExterno.isBlank()) {
                log.warn("Twin reported ignorado: idExterno no presente en topic. topic={}", topic);
                return;
            }

            var disp = dispositivoRepository.findByIdExternoIgnoreCase(idExterno)
                    .orElse(null);
            if (disp == null) {
                log.warn("Twin reported: dispositivo no encontrado. idExterno={} topic={}", idExterno, topic);
                return;
            }

            Map<String, Object> patch = objectMapper.readValue(payload, new TypeReference<>() {});
            twinService.updateReported(disp.getId(), patch);
        } catch (Exception e) {
            log.error("Error procesando twin reported MQTT. topic={} payload={}", topic, payload, e);
        }
    }
}

