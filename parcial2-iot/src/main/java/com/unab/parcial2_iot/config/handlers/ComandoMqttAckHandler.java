package com.unab.parcial2_iot.config.handlers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.unab.parcial2_iot.models.EstadoComando;
import com.unab.parcial2_iot.services.ComandoDispositivoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.integration.mqtt.support.MqttHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessagingException;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class ComandoMqttAckHandler implements MessageHandler {

    private final ObjectMapper objectMapper;
    private final ComandoDispositivoService comandoService;

    @Override
    public void handleMessage(Message<?> message) throws MessagingException {
        String topic = (String) message.getHeaders().get(MqttHeaders.RECEIVED_TOPIC);
        String payload = String.valueOf(message.getPayload());

        try {
            JsonNode root = objectMapper.readTree(payload);

            if (!root.hasNonNull("id")) {
                log.warn("ACK ignorado: falta id de comando. topic={} payload={}", topic, payload);
                return;
            }

            UUID id = UUID.fromString(root.get("id").asText());

            EstadoComando estado = EstadoComando.ACK; // por defecto
            if (root.hasNonNull("estado")) {
                try {
                    estado = EstadoComando.valueOf(root.get("estado").asText().trim().toUpperCase());
                } catch (IllegalArgumentException iae) {
                    // dejar ACK por defecto
                    log.warn("Estado ACK no reconocido, usando ACK por defecto. value={} id={}", root.get("estado").asText(), id);
                }
            }

            String mensajeError = null;
            if (root.hasNonNull("mensajeError")) {
                mensajeError = root.get("mensajeError").asText();
            } else if (estado == EstadoComando.ERROR && root.hasNonNull("error")) {
                mensajeError = root.get("error").asText();
            }

            comandoService.cambiarEstado(id, estado, mensajeError);
        } catch (Exception e) {
            log.error("Error procesando ACK MQTT. topic={} payload={}", topic, payload, e);
        }
    }
}

