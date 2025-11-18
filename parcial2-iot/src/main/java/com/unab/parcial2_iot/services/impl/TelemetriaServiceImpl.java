package com.unab.parcial2_iot.services.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.unab.parcial2_iot.dto.EstadoDispositivoOut;
import com.unab.parcial2_iot.dto.TelemetriaIn;
import com.unab.parcial2_iot.dto.TelemetriaOutMin;
import com.unab.parcial2_iot.models.Dispositivo;
import com.unab.parcial2_iot.models.Telemetria;
import com.unab.parcial2_iot.models.TipoDato;
import com.unab.parcial2_iot.models.VariablePlantilla;
import com.unab.parcial2_iot.repositories.DispositivoRepository;
import com.unab.parcial2_iot.repositories.EstadoDispositivoRepository;
import com.unab.parcial2_iot.repositories.TelemetriaRepository;
import com.unab.parcial2_iot.repositories.VariablePlantillaRepository;
import com.unab.parcial2_iot.services.SseHub;
import com.unab.parcial2_iot.services.TelemetriaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;


@Service
@RequiredArgsConstructor
@Transactional
public class TelemetriaServiceImpl implements TelemetriaService {

    private final DispositivoRepository dispositivoRepo;
    private final VariablePlantillaRepository varRepo;
    private final TelemetriaRepository telemetriaRepo;
    private final EstadoDispositivoRepository estadoRepo;
    private final SseHub sseHub;
    private final com.unab.parcial2_iot.services.TelemetryRouter telemetryRouter;
    private final ObjectMapper objectMapper;
    @PersistenceContext
    private EntityManager entityManager;
    private final com.unab.parcial2_iot.services.ReglaService reglaService;

    @Override
    public void registrarLectura(TelemetriaIn in) {
        // 1) Dispositivo
        Dispositivo disp = resolverDispositivo(in);

        // 2) Variable (pertenencia a la misma plantilla)
        VariablePlantilla var = resolverVariable(disp, in);

        // 3) Normalización: si se esperaba booleano y llega número 0/1, convertir
        if (var.getTipoDato() == TipoDato.booleano && in.getBooleano() == null && in.getNumero() != null) {
            double v = in.getNumero();
            if (v == 0d || v == 1d) {
                in.setBooleano(v == 1d);
                in.setNumero(null);
            }
        }

        // Debe venir solo un valor
        int count = nonNull(in.getNumero()) + nonNull(in.getBooleano()) + nonNull(in.getTexto()) + nonNull(in.getJson());
        if (count != 1) {
            throw new IllegalArgumentException("Debe enviar exactamente un valor: numero | booleano | texto | json");
        }

        // 4) Validar tipo_dato
        TipoDato esperado = var.getTipoDato();
        switch (esperado) {
            case numero   -> { if (in.getNumero()   == null) throw tipoErr("numero"); }
            case booleano -> { if (in.getBooleano() == null) throw tipoErr("booleano"); }
            case cadena   -> { if (in.getTexto()    == null) throw tipoErr("cadena"); }
            case json     -> { if (in.getJson()     == null) throw tipoErr("json"); }
        }

        // 5) Construir entidad
        Telemetria t = new Telemetria();
        t.setDispositivo(disp);
        t.setVariable(var); // <-- FIX aquí (antes: setVariablePlantilla)
        t.setTs(in.getTs() != null ? in.getTs() : OffsetDateTime.now());
        t.setEtiquetas(in.getEtiquetas());

        // Setear SOLO el campo correcto
        switch (esperado) {
            case numero   -> t.setValorNumeroSeguro(in.getNumero());
            case booleano -> t.setValorBooleanoSeguro(in.getBooleano());
            case cadena   -> t.setValorTextoSeguro(in.getTexto());
            case json     -> t.setValorJsonSeguro((Map<String, Object>) in.getJson());
        }

        // 6) Guardar (trigger actualiza estado_dispositivo)
        telemetriaRepo.save(t);
        try { entityManager.flush(); } catch (Exception ignored) {}
        try { reglaService.evaluarPara(disp.getId(), var.getId()); } catch (Exception ignored) {}
        try { telemetryRouter.route(t); } catch (Exception ignored) {}

        // 7) Notificar por SSE: estado y telemetría
        estadoRepo.findOneWithJoins(disp.getId(), var.getId()).ifPresent(e -> {
            EstadoDispositivoOut dto = EstadoDispositivoOut.builder()
                    .dispositivoId(e.getDispositivo().getId())
                    .dispositivoNombre(e.getDispositivo().getNombre())
                    .variableId(e.getVariable().getId())
                    .variableNombre(e.getVariable().getNombre())
                    .variableEtiqueta(e.getVariable().getEtiqueta())
                    .ultimoTs(e.getUltimoTs())
                    .ultimoNumero(e.getUltimoNumero())
                    .ultimoBooleano(e.getUltimoBooleano())
                    .ultimoTexto(e.getUltimoTexto())
                    .ultimoJson(e.getUltimoJson())
                    .build();
            sseHub.broadcastEstado(dto);
        });

        String jsonStr = null;
        if (in.getJson() != null) {
            try { jsonStr = objectMapper.writeValueAsString(in.getJson()); } catch (Exception ignored) {}
        }
        TelemetriaOutMin out = TelemetriaOutMin.builder()
                .dispositivoId(disp.getId())
                .dispositivoNombre(disp.getNombre())
                .variableId(var.getId())
                .variableNombre(var.getEtiqueta() != null ? var.getEtiqueta() : var.getNombre())
                .ts(t.getTs())
                .numero(in.getNumero())
                .booleano(in.getBooleano())
                .texto(in.getTexto())
                .json(jsonStr)
                .build();
        sseHub.broadcastTelemetria(out);
    }

    private Dispositivo resolverDispositivo(TelemetriaIn in) {
        if (in.getDispositivoId() != null) {
            return dispositivoRepo.findById(in.getDispositivoId())
                    .orElseThrow(() -> new IllegalArgumentException("Dispositivo no encontrado"));
        }
        if (in.getIdExterno() != null && !in.getIdExterno().isBlank()) {
            return dispositivoRepo.findByIdExternoIgnoreCase(in.getIdExterno().trim())
                    .orElseThrow(() -> new IllegalArgumentException("Dispositivo con id_externo no encontrado"));
        }
        throw new IllegalArgumentException("Debe indicar dispositivoId o idExterno");
    }

    private VariablePlantilla resolverVariable(Dispositivo disp, TelemetriaIn in) {
        VariablePlantilla var;
        if (in.getVariableId() != null) {
            var = varRepo.findById(in.getVariableId())
                    .orElseThrow(() -> new IllegalArgumentException("Variable no encontrada"));
        } else if (in.getVariableNombre() != null && !in.getVariableNombre().isBlank()) {
            var = varRepo.findByPlantilla_IdAndNombreIgnoreCase(disp.getPlantilla().getId(),
                            in.getVariableNombre().trim())
                    .orElseThrow(() -> new IllegalArgumentException("Variable no encontrada en la plantilla del dispositivo"));
        } else {
            throw new IllegalArgumentException("Debe indicar variableId o variableNombre");
        }

        // Pertenencia (defensivo)
        UUID pidDisp = disp.getPlantilla().getId();
        UUID pidVar  = var.getPlantilla().getId();
        if (!Objects.equals(pidDisp, pidVar)) {
            throw new IllegalArgumentException("La variable no pertenece a la plantilla del dispositivo");
        }
        return var;
    }

    private IllegalArgumentException tipoErr(String esperado) {
        return new IllegalArgumentException("Tipo de valor inválido. Se esperaba: " + esperado);
    }

    private int nonNull(Object o) { return o == null ? 0 : 1; }
}
