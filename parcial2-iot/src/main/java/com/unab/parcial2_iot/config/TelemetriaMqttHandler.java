package com.unab.parcial2_iot.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.unab.parcial2_iot.dto.TelemetriaIn;
import com.unab.parcial2_iot.services.TelemetriaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessagingException;
import org.springframework.integration.mqtt.support.MqttHeaders;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class TelemetriaMqttHandler implements MessageHandler {

    private final TelemetriaService telemetriaService;
    private final ObjectMapper objectMapper;

    @Override
    public void handleMessage(Message<?> message) throws MessagingException {
        String topic   = (String) message.getHeaders().get(MqttHeaders.RECEIVED_TOPIC);
        String payload = String.valueOf(message.getPayload());

        try {
            // topic esperado: devices/{idExterno}/telemetry[/variable?]
            String[] parts = topic.split("/");
            String idExterno = parts.length >= 2 ? parts[1] : null;
            String varFromTopic = (parts.length >= 4) ? parts[3] : null;

            JsonNode root = objectMapper.readTree(payload);

            TelemetriaIn in = new TelemetriaIn();

            // id del dispositivo (prioridad: body > topic)
            if (root.hasNonNull("idExterno")) {
                in.setIdExterno(root.get("idExterno").asText());
            } else {
                in.setIdExterno(idExterno);
            }
            if (root.hasNonNull("dispositivoId")) {
                in.setDispositivoId(UUID.fromString(root.get("dispositivoId").asText()));
            }

            // variable (prioridad: body > topic)
            if (root.hasNonNull("variableId")) {
                in.setVariableId(UUID.fromString(root.get("variableId").asText()));
            }
            if (root.hasNonNull("variableNombre")) {
                in.setVariableNombre(root.get("variableNombre").asText());
            } else if (varFromTopic != null) {
                in.setVariableNombre(varFromTopic);
            }

            // valores (exactamente uno)
            if (root.has("numero"))   in.setNumero(root.get("numero").asDouble());
            if (root.has("booleano")) in.setBooleano(root.get("booleano").asBoolean());
            if (root.has("texto"))    in.setTexto(root.get("texto").asText());
            if (root.has("json")) {
                Map<String,Object> m = objectMapper.convertValue(root.get("json"), new TypeReference<Map<String,Object>>(){});
                in.setJson(m);
            }

            // timestamp y etiquetas
            if (root.hasNonNull("ts")) {
                in.setTs(OffsetDateTime.parse(root.get("ts").asText()));
            }
            if (root.has("etiquetas")) {
                Map<String,Object> tags = objectMapper.convertValue(root.get("etiquetas"), new TypeReference<Map<String,Object>>(){});
                in.setEtiquetas(tags);
            }

            // etiqueta de origen útil para auditoría
            if (in.getEtiquetas() == null) in.setEtiquetas(new java.util.HashMap<>());
            in.getEtiquetas().putIfAbsent("origen", "mqtt");
            in.getEtiquetas().putIfAbsent("topic", topic);

            telemetriaService.registrarLectura(in); // reutilizamos la validación que ya tienes
        } catch (Exception e) {
            log.error("Error procesando MQTT. topic={} payload={}", topic, payload, e);
            // aquí podrías enviar a una DLQ (otra topic) si lo deseas
        }
    }
}
