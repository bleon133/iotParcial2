package com.unab.parcial2_iot.controller.api;

import com.unab.parcial2_iot.services.SseHub;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
public class SseController {

    private final SseHub hub;

    @GetMapping("/estado")
    public SseEmitter estado(@RequestParam(name = "dispositivoId", required = false) UUID dispositivoId) {
        return hub.subscribeEstado(dispositivoId);
    }

    @GetMapping("/telemetria")
    public SseEmitter telemetria(@RequestParam(name = "dispositivoId") String dispositivoIdStr,
                                 @RequestParam(name = "variableId") String variableIdStr) {
        java.util.UUID dispositivoId = null, variableId = null;
        try {
            if (dispositivoIdStr != null && !dispositivoIdStr.isBlank() && !"null".equalsIgnoreCase(dispositivoIdStr)) {
                dispositivoId = java.util.UUID.fromString(dispositivoIdStr.trim());
            }
            if (variableIdStr != null && !variableIdStr.isBlank() && !"null".equalsIgnoreCase(variableIdStr)) {
                variableId = java.util.UUID.fromString(variableIdStr.trim());
            }
        } catch (Exception ignored) { /* invalid UUID handled below */ }
        if (dispositivoId == null || variableId == null) {
            throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.BAD_REQUEST, "Parametros dispositivoId y variableId requeridos");
        }
        return hub.subscribeTelemetria(dispositivoId, variableId);
    }

    @GetMapping("/comandos")
    public SseEmitter comandos(@RequestParam(name = "dispositivoId", required = false) UUID dispositivoId) {
        return hub.subscribeComandos(dispositivoId);
    }

    @GetMapping("/alertas")
    public SseEmitter alertas(@RequestParam(name = "tipo", required = false) String tipo,
                              @RequestParam(name = "id", required = false) UUID id) {
        return hub.subscribeAlertas(tipo, id);
    }
}
