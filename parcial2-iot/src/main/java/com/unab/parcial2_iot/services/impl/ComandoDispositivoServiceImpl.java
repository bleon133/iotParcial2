package com.unab.parcial2_iot.services.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.unab.parcial2_iot.models.ComandoDispositivo;
import com.unab.parcial2_iot.models.Dispositivo;
import com.unab.parcial2_iot.models.EstadoComando;
import com.unab.parcial2_iot.repositories.ComandoDispositivoRepository;
import com.unab.parcial2_iot.repositories.DispositivoRepository;
import com.unab.parcial2_iot.services.ComandoDispositivoService;
import com.unab.parcial2_iot.services.MqttPublishService;
import com.unab.parcial2_iot.services.SseHub;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ComandoDispositivoServiceImpl implements ComandoDispositivoService {

    private final ComandoDispositivoRepository comandoRepo;
    private final DispositivoRepository dispositivoRepo;
    private final ObjectMapper objectMapper;
    private final MqttPublishService mqtt;
    private final SseHub sseHub;

    @Override
    public ComandoDispositivo crear(UUID dispositivoId,
                                    String comando,
                                    String datos,
                                    String solicitadoPor) {

        var disp = dispositivoRepo.findById(dispositivoId)
                .orElseThrow(() -> new EntityNotFoundException("Dispositivo no encontrado"));

        String datosJson = (datos == null || datos.isBlank()) ? null : datos;

        ComandoDispositivo cmd = ComandoDispositivo.builder()
                .dispositivo(disp)
                .comando(comando)
                .datos(datosJson)                    // debe ser JSON válido si no es null
                .estado(EstadoComando.ENVIADO)       // ahora sí mapea al enum PG
                .solicitadoPor(solicitadoPor)
                .solicitadoEn(OffsetDateTime.now())
                .build();

        cmd = comandoRepo.save(cmd);

        // SSE: notificar comando en estado ENVIADO
        try {
            var dto = com.unab.parcial2_iot.dto.ComandoUpdateOut.builder()
                    .id(cmd.getId())
                    .dispositivoId(disp.getId())
                    .dispositivoNombre(disp.getNombre())
                    .estado(cmd.getEstado())
                    .solicitadoEn(cmd.getSolicitadoEn())
                    .comando(cmd.getComando())
                    .datos(cmd.getDatos())
                    .confirmadoEn(cmd.getConfirmadoEn())
                    .mensajeError(cmd.getMensajeError())
                    .build();
            sseHub.broadcastComando(dto);
        } catch (Exception ignored) { }

        // Publicar por MQTT (best-effort)
        String topic = disp.getTopicoMqttComando();
        if (topic == null || topic.isBlank()) {
            topic = "devices/" + disp.getIdExterno() + "/commands";
        }
        try {
            var payloadMap = new LinkedHashMap<String, Object>();
            payloadMap.put("id", cmd.getId().toString());
            payloadMap.put("dispositivoId", disp.getId().toString());
            payloadMap.put("idExterno", disp.getIdExterno());
            payloadMap.put("comando", comando);
            if (datosJson != null && !datosJson.isBlank()) {
                payloadMap.put("datos", objectMapper.readTree(datosJson));
            }
            payloadMap.put("solicitadoPor", solicitadoPor);
            payloadMap.put("solicitadoEn", cmd.getSolicitadoEn().toString());
            String payload = objectMapper.writeValueAsString(payloadMap);
            mqtt.publish(topic, payload);
        } catch (Exception e) {
            log.warn("No se pudo publicar comando por MQTT. topic={} id={} error={}", topic, cmd.getId(), e.toString());
        }

        return cmd;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ComandoDispositivo> historialPorDispositivo(UUID dispositivoId) {
        if (dispositivoId != null) {
            return comandoRepo.findByDispositivoIdOrderBySolicitadoEnDesc(dispositivoId);
        }
        return comandoRepo.findAllByOrderBySolicitadoEnDesc();
    }

    @Override
    public ComandoDispositivo cambiarEstado(UUID comandoId,
                                            EstadoComando nuevoEstado,
                                            String mensajeError) {

        ComandoDispositivo cmd = comandoRepo.findById(comandoId)
                .orElseThrow(() -> new EntityNotFoundException("Comando no encontrado"));

        cmd.setEstado(nuevoEstado);

        if (nuevoEstado == EstadoComando.ACK
                || nuevoEstado == EstadoComando.ERROR
                || nuevoEstado == EstadoComando.TIEMPO_AGOTADO
                || nuevoEstado == EstadoComando.CANCELADO) {
            cmd.setConfirmadoEn(OffsetDateTime.now());
        }

        if (mensajeError != null && !mensajeError.isBlank()) {
            cmd.setMensajeError(mensajeError);
        }

        cmd = comandoRepo.save(cmd);

        // SSE: notificar cambio de estado
        try {
            var dto = com.unab.parcial2_iot.dto.ComandoUpdateOut.builder()
                    .id(cmd.getId())
                    .dispositivoId(cmd.getDispositivo().getId())
                    .dispositivoNombre(cmd.getDispositivo().getNombre())
                    .estado(cmd.getEstado())
                    .confirmadoEn(cmd.getConfirmadoEn())
                    .mensajeError(cmd.getMensajeError())
                    .build();
            sseHub.broadcastComando(dto);
        } catch (Exception ignored) { }

        return cmd;
    }

}
