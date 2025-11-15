package com.unab.parcial2_iot.services;

import com.unab.parcial2_iot.dto.EstadoDispositivoOut;
import com.unab.parcial2_iot.dto.TelemetriaOutMin;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SseHub {

    private static final long TIMEOUT_MS = Duration.ofMinutes(30).toMillis();

    private final Map<String, List<SseEmitter>> emitters = new ConcurrentHashMap<>();

    private static String keyEstado(java.util.UUID dispositivoId) {
        return dispositivoId == null ? "estado_all" : ("estado_" + dispositivoId);
    }

    private static String keyTele(java.util.UUID dispositivoId, java.util.UUID variableId) {
        return "tele_" + dispositivoId + "_" + variableId;
    }

    private static String keyCmd(java.util.UUID dispositivoId) {
        return dispositivoId == null ? "cmd_all" : ("cmd_" + dispositivoId);
    }

    private static String keyAlertAll() { return "alert_all"; }
    private static String keyAlertRegla(java.util.UUID reglaId) { return "alert_regla_" + reglaId; }
    private static String keyAlertDisp(java.util.UUID dispId) { return "alert_disp_" + dispId; }

    public SseEmitter subscribeEstado(java.util.UUID dispositivoId) {
        String key = keyEstado(dispositivoId);
        SseEmitter emitter = new SseEmitter(TIMEOUT_MS);
        addEmitter(key, emitter);
        return emitter;
    }

    public SseEmitter subscribeTelemetria(java.util.UUID dispositivoId, java.util.UUID variableId) {
        String key = keyTele(dispositivoId, variableId);
        SseEmitter emitter = new SseEmitter(TIMEOUT_MS);
        addEmitter(key, emitter);
        return emitter;
    }

    public void broadcastEstado(EstadoDispositivoOut dto) {
        if (dto == null) return;
        var targets = new ArrayList<String>();
        targets.add(keyEstado(null));
        if (dto.getDispositivoId() != null) targets.add(keyEstado(dto.getDispositivoId()));
        for (String key : targets) sendAll(key, dto);
    }

    public void broadcastTelemetria(TelemetriaOutMin dto) {
        if (dto == null || dto.getDispositivoId() == null || dto.getVariableId() == null) return;
        sendAll(keyTele(dto.getDispositivoId(), dto.getVariableId()), dto);
    }

    public SseEmitter subscribeComandos(java.util.UUID dispositivoId) {
        String key = keyCmd(dispositivoId);
        SseEmitter emitter = new SseEmitter(TIMEOUT_MS);
        addEmitter(key, emitter);
        return emitter;
    }

    public void broadcastComando(com.unab.parcial2_iot.dto.ComandoUpdateOut dto) {
        if (dto == null) return;
        var targets = new ArrayList<String>();
        targets.add(keyCmd(null));
        if (dto.getDispositivoId() != null) targets.add(keyCmd(dto.getDispositivoId()));
        for (String key : targets) sendAll(key, dto);
    }

    public SseEmitter subscribeAlertas(String tipo, java.util.UUID id) {
        String key = keyAlertAll();
        if ("regla".equalsIgnoreCase(tipo) && id != null) key = keyAlertRegla(id);
        else if ("dispositivo".equalsIgnoreCase(tipo) && id != null) key = keyAlertDisp(id);
        SseEmitter emitter = new SseEmitter(TIMEOUT_MS);
        addEmitter(key, emitter);
        return emitter;
    }

    public void broadcastAlerta(com.unab.parcial2_iot.dto.AlertaOut dto) {
        if (dto == null) return;
        sendAll(keyAlertAll(), dto);
        if (dto.getReglaId() != null) sendAll(keyAlertRegla(dto.getReglaId()), dto);
        if (dto.getDispositivoId() != null) sendAll(keyAlertDisp(dto.getDispositivoId()), dto);
    }

    private void addEmitter(String key, SseEmitter emitter) {
        emitters.computeIfAbsent(key, k -> new ArrayList<>()).add(emitter);
        emitter.onCompletion(() -> removeEmitter(key, emitter));
        emitter.onTimeout(() -> removeEmitter(key, emitter));
        emitter.onError(e -> removeEmitter(key, emitter));
        try {
            emitter.send(SseEmitter.event().name("init").data("ok"));
        } catch (IOException ignored) {}
    }

    private void removeEmitter(String key, SseEmitter emitter) {
        var list = emitters.get(key);
        if (list != null) list.removeIf(e -> Objects.equals(e, emitter));
    }

    private void sendAll(String key, Object payload) {
        var list = emitters.get(key);
        if (list == null || list.isEmpty()) return;
        List<SseEmitter> dead = new ArrayList<>();
        for (SseEmitter e : list) {
            try {
                e.send(SseEmitter.event().name("update").data(payload));
            } catch (IOException ex) {
                // Cliente cerró la conexión: marcar para remover y completar el emitter
                try { e.complete(); } catch (Exception ignore) {}
                dead.add(e);
            }
        }
        if (!dead.isEmpty()) list.removeAll(dead);
    }
}
