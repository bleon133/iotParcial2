package com.unab.parcial2_iot.services.impl;

import com.unab.parcial2_iot.dto.ReglaIn;
import com.unab.parcial2_iot.models.Dispositivo;
import com.unab.parcial2_iot.models.Regla;
import com.unab.parcial2_iot.models.VariablePlantilla;
import com.unab.parcial2_iot.repositories.*;
import com.unab.parcial2_iot.services.AlertaService;
import com.unab.parcial2_iot.services.ReglaService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReglaServiceImpl implements ReglaService {

    private final ReglaRepository reglaRepo;
    private final VariablePlantillaRepository varRepo;
    private final DispositivoRepository dispositivoRepo;
    private final TelemetriaRepository teleRepo;
    private final AlertaService alertaService;

    // Formato aceptado de expresión:
    //   avg > 50
    //   avg_5m > 50
    //   min < 20
    //   max >= 90
    //   avg Temperatura > 50  <-- ahora permitido
    private static final Pattern EXP = Pattern.compile(
            "(?i)^(avg|min|max)(?:\\s+\\w+)?(?:_(\\d+)([smh]))?\\s*(>=|<=|>|<|==|=)\\s*([+-]?\\d+(?:\\.\\d+)?)$"
    );

    @Override
    @Transactional
    public Regla crear(ReglaIn in) {
        VariablePlantilla var = varRepo.findById(in.getVariableId())
                .orElseThrow(() -> new IllegalArgumentException("VariablePlantilla no existe"));

        Regla r = Regla.builder()
                .variable(var)
                .nombre(in.getNombre())
                .expresion(in.getExpresion().trim())
                .severidad(in.getSeveridad() == null ? "info" : in.getSeveridad())
                .habilitada(in.getHabilitada() == null ? Boolean.TRUE : in.getHabilitada())
                .ventanaSegundos(in.getVentanaSegundos() == null ? 300 : in.getVentanaSegundos())
                .creadaEn(OffsetDateTime.now())
                .build();

        return reglaRepo.save(r);
    }

    @Override
    @Transactional
    public Regla actualizar(UUID id, ReglaIn in) {
        Regla r = reglaRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Regla no existe"));

        VariablePlantilla var = varRepo.findById(in.getVariableId())
                .orElseThrow(() -> new IllegalArgumentException("VariablePlantilla no existe"));

        r.setVariable(var);
        r.setNombre(in.getNombre());
        r.setExpresion(in.getExpresion().trim());
        r.setSeveridad(in.getSeveridad());
        r.setHabilitada(in.getHabilitada());
        r.setVentanaSegundos(in.getVentanaSegundos());
        return reglaRepo.save(r);
    }

    @Override
    @Transactional
    public void eliminar(UUID id) {
        reglaRepo.deleteById(id);
    }

    @Override
    public List<Regla> listarTodas() {
        return reglaRepo.findAll();
    }

    @Override
    public List<Regla> listarHabilitadas() {
        return reglaRepo.findByHabilitadaTrue();
    }

    @Scheduled(fixedDelayString = "${app.rules.fixedDelay:60000}")
    @Override
    @Transactional
    public void evaluarReglas() {
        List<Regla> activas = reglaRepo.findByHabilitadaTrue();
        if (activas.isEmpty()) return;

        for (Regla r : activas) {
            try {
                EvalSpec spec = parseExpresion(r);
                int ventanaSeg = (spec.overrideVentanaSegundos != null)
                        ? spec.overrideVentanaSegundos
                        : (r.getVentanaSegundos() == null ? 300 : r.getVentanaSegundos());

                OffsetDateTime since = OffsetDateTime.now().minusSeconds(ventanaSeg);

                UUID plantillaId = r.getVariable().getPlantilla().getId();
                List<Dispositivo> dispositivos = dispositivoRepo.findByPlantilla_Id(plantillaId);

                for (Dispositivo d : dispositivos) {
                    TelemetriaRepository.AggRow agg =
                            teleRepo.agg(d.getId(), r.getVariable().getId(), since);

                    if (agg == null || agg.getCnt() == null || agg.getCnt() == 0) continue;

                    Double valor = switch (spec.metric) {
                        case "avg" -> agg.getAvg();
                        case "min" -> agg.getMin();
                        case "max" -> agg.getMax();
                        default -> null;
                    };
                    if (valor == null) continue;

                    boolean cumple = comparar(valor, spec.op, spec.umbral);
                    if (cumple) {
                        Map<String, Object> det = new LinkedHashMap<>();
                        det.put("metric", spec.metric);
                        det.put("valor", valor);
                        det.put("umbral", spec.umbral);
                        det.put("operador", spec.op);
                        det.put("ventanaSegundos", ventanaSeg);
                        det.put("variableId", r.getVariable().getId());
                        det.put("dispositivoId", d.getId());
                        det.put("reglaId", r.getId());
                        alertaService.crear(d, r, det);
                    }
                }
            } catch (Exception e) {
                log.error("Error evaluando regla {}: {}", r.getId(), e.getMessage(), e);
            }
        }
    }

    // ---- Helpers ----
    private record EvalSpec(String metric, String op, double umbral, Integer overrideVentanaSegundos) {}

    private EvalSpec parseExpresion(Regla r) {
        String expr = r.getExpresion().trim();
        Matcher m = EXP.matcher(expr);
        if (!m.matches()) {
            throw new IllegalArgumentException("Expresión no válida: " + expr +
                    " (usa: avg|min|max[_5m opcional] operadores >,<,>=,<=,=,== y un número)");
        }
        String metric = m.group(1).toLowerCase();
        String num = m.group(2);
        String unit = m.group(3);
        String op = m.group(4);
        double umbral = Double.parseDouble(m.group(5));

        Integer overrideWindow = null;
        if (num != null && unit != null) {
            int n = Integer.parseInt(num);
            switch (unit.toLowerCase()) {
                case "s" -> overrideWindow = n;
                case "m" -> overrideWindow = n * 60;
                case "h" -> overrideWindow = n * 3600;
            }
        }
        return new EvalSpec(metric, op, umbral, overrideWindow);
    }

    private boolean comparar(Double v, String op, double umbral) {
        return switch (op) {
            case ">"  -> v > umbral;
            case "<"  -> v < umbral;
            case ">=" -> v >= umbral;
            case "<=" -> v <= umbral;
            case "=", "==" -> Objects.equals(v, umbral);
            default -> false;
        };
    }
}
