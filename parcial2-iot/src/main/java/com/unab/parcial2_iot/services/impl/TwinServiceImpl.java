package com.unab.parcial2_iot.services.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.unab.parcial2_iot.dto.TwinOut;
import com.unab.parcial2_iot.models.DeviceTwin;
import com.unab.parcial2_iot.models.Dispositivo;
import com.unab.parcial2_iot.repositories.DeviceTwinRepository;
import com.unab.parcial2_iot.repositories.DispositivoRepository;
import com.unab.parcial2_iot.services.MqttPublishService;
import com.unab.parcial2_iot.services.TwinService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class TwinServiceImpl implements TwinService {

    private final DeviceTwinRepository twinRepo;
    private final DispositivoRepository dispositivoRepo;
    private final ObjectMapper objectMapper;
    private final MqttPublishService mqtt;

    @Override
    @Transactional(readOnly = true)
    public TwinOut getTwin(UUID dispositivoId) {
        Dispositivo disp = dispositivoRepo.findById(dispositivoId)
                .orElseThrow(() -> new EntityNotFoundException("Dispositivo no encontrado"));
        DeviceTwin twin = twinRepo.findByDispositivoId(dispositivoId)
                .orElse(DeviceTwin.builder()
                        .dispositivoId(dispositivoId)
                        .desired(new HashMap<>())
                        .reported(new HashMap<>())
                        .version(0)
                        .updatedAt(OffsetDateTime.now())
                        .build());
        return toOut(disp, twin);
    }

    @Override
    public TwinOut patchDesired(UUID dispositivoId, Map<String, Object> desiredPatch) {
        Dispositivo disp = dispositivoRepo.findById(dispositivoId)
                .orElseThrow(() -> new EntityNotFoundException("Dispositivo no encontrado"));
        DeviceTwin twin = twinRepo.findByDispositivoId(dispositivoId)
                .orElseGet(() -> DeviceTwin.builder()
                        .dispositivoId(dispositivoId)
                        .desired(new HashMap<>())
                        .reported(new HashMap<>())
                        .version(0)
                        .updatedAt(OffsetDateTime.now())
                        .build());

        Map<String, Object> desired = new HashMap<>(twin.getDesired() == null ? new HashMap<>() : twin.getDesired());
        deepMerge(desired, desiredPatch);
        twin.setDesired(desired);
        // updated_at y version se manejan por trigger; actualizamos para evitar nulls en primeras inserciones
        twin.setUpdatedAt(OffsetDateTime.now());
        twin = twinRepo.save(twin);

        // Publicar desired al dispositivo por MQTT (best-effort)
        try {
            String topic = "devices/" + disp.getIdExterno() + "/twin/desired";
            String payload = objectMapper.writeValueAsString(twin.getDesired());
            mqtt.publish(topic, payload);
        } catch (Exception e) {
            log.warn("No se pudo publicar desired del twin. dispositivoId={} error={}", dispositivoId, e.toString());
        }

        return toOut(disp, twin);
    }

    @Override
    public TwinOut updateReported(UUID dispositivoId, Map<String, Object> reportedPatch) {
        Dispositivo disp = dispositivoRepo.findById(dispositivoId)
                .orElseThrow(() -> new EntityNotFoundException("Dispositivo no encontrado"));
        DeviceTwin twin = twinRepo.findByDispositivoId(dispositivoId)
                .orElseGet(() -> DeviceTwin.builder()
                        .dispositivoId(dispositivoId)
                        .desired(new HashMap<>())
                        .reported(new HashMap<>())
                        .version(0)
                        .updatedAt(OffsetDateTime.now())
                        .build());

        Map<String, Object> reported = new HashMap<>(twin.getReported() == null ? new HashMap<>() : twin.getReported());
        deepMerge(reported, reportedPatch);
        twin.setReported(reported);
        twin.setUpdatedAt(OffsetDateTime.now());
        twin = twinRepo.save(twin);

        return toOut(disp, twin);
    }

    private TwinOut toOut(Dispositivo d, DeviceTwin t) {
        return TwinOut.builder()
                .dispositivoId(d.getId())
                .dispositivoNombre(d.getNombre())
                .idExterno(d.getIdExterno())
                .desired(t.getDesired())
                .reported(t.getReported())
                .version(t.getVersion())
                .updatedAt(t.getUpdatedAt())
                .build();
    }

    @SuppressWarnings("unchecked")
    private void deepMerge(Map<String, Object> base, Map<String, Object> patch) {
        if (patch == null) return;
        for (Map.Entry<String, Object> e : patch.entrySet()) {
            String key = e.getKey();
            Object val = e.getValue();
            if (val == null) {
                base.remove(key);
            } else {
                Object curr = base.get(key);
                if (curr instanceof Map<?,?> cMap && val instanceof Map<?,?> vMap) {
                    Map<String,Object> cm = new HashMap<>((Map<String,Object>) cMap);
                    deepMerge(cm, (Map<String,Object>) vMap);
                    base.put(key, cm);
                } else {
                    base.put(key, val);
                }
            }
        }
    }
}

