package com.unab.parcial2_iot.services.impl;

import com.unab.parcial2_iot.dto.ReglaIn;
import com.unab.parcial2_iot.models.Dispositivo;
import com.unab.parcial2_iot.models.Regla;
import com.unab.parcial2_iot.models.VariablePlantilla;
import com.unab.parcial2_iot.repositories.*;
import com.unab.parcial2_iot.services.AlertaService;
import com.unab.parcial2_iot.services.ReglaService;
import jakarta.transaction.Transactional;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
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
    @PersistenceContext
    private EntityManager entityManager;

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

        java.util.Map<String,Object> cfg = new java.util.HashMap<>();
        boolean hasBands = in.getBandsHighValue()!=null || in.getBandsLowValue()!=null || in.getBandsNormalMin()!=null || in.getBandsNormalMax()!=null;
        String tipo = (in.getTipo()==null || in.getTipo().isBlank()) ? (hasBands?"bands":"expr") : in.getTipo().trim().toLowerCase();
        if ("bands".equals(tipo)) {
            cfg.put("metric", "avg");
            if (in.getBandsHighValue()!=null) cfg.put("high", java.util.Map.of("op", ">", "value", in.getBandsHighValue(), "severity", java.util.Objects.toString(in.getBandsHighSeverity(), "grave")));
            if (in.getBandsLowValue()!=null) cfg.put("low", java.util.Map.of("op", "<", "value", in.getBandsLowValue(), "severity", java.util.Objects.toString(in.getBandsLowSeverity(), "bajo")));
            java.util.Map<String,Object> normal = new java.util.HashMap<>();
            if (in.getBandsNormalMin()!=null) normal.put("min", in.getBandsNormalMin());
            if (in.getBandsNormalMax()!=null) normal.put("max", in.getBandsNormalMax());
            if (!normal.isEmpty()) cfg.put("normal", normal);
        }
        String exprIn = in.getExpresion()==null? null : in.getExpresion().trim();
        if ("expr".equals(tipo) && (exprIn==null || exprIn.isBlank())) {
            throw new IllegalArgumentException("Debe ingresar una expresión o configurar bandas");
        }

        Regla r = Regla.builder()
                .variable(var)
                .dispositivo(in.getDispositivoId()!=null ? dispositivoRepo.findById(in.getDispositivoId()).orElse(null) : null)
                .nombre(in.getNombre())
                .expresion(exprIn!=null && !exprIn.isBlank() ? exprIn : "avg >= -1e18")
                .severidad(in.getSeveridad() == null ? "info" : in.getSeveridad())
                .habilitada(in.getHabilitada() == null ? Boolean.TRUE : in.getHabilitada())
                .ventanaSegundos(in.getVentanaSegundos() == null ? 300 : in.getVentanaSegundos())
                .creadaEn(OffsetDateTime.now())
                .tipo(tipo)
                .config(cfg)
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
        if (in.getDispositivoId()!=null) {
            r.setDispositivo(dispositivoRepo.findById(in.getDispositivoId()).orElse(null));
        } else {
            r.setDispositivo(null);
        }
        r.setNombre(in.getNombre());
        boolean hasBandsUp = in.getBandsHighValue()!=null || in.getBandsLowValue()!=null || in.getBandsNormalMin()!=null || in.getBandsNormalMax()!=null;
        String tipoUp = (in.getTipo()==null||in.getTipo().isBlank())? (hasBandsUp?"bands":r.getTipo()) : in.getTipo().trim().toLowerCase();
        String exprUp = in.getExpresion()==null? null : in.getExpresion().trim();
        if ("expr".equals(tipoUp) && (exprUp==null || exprUp.isBlank())) {
            throw new IllegalArgumentException("Debe ingresar una expresión o configurar bandas");
        }
        r.setExpresion(exprUp!=null && !exprUp.isBlank() ? exprUp : "avg >= -1e18");
        r.setSeveridad(in.getSeveridad());
        r.setHabilitada(in.getHabilitada());
        r.setVentanaSegundos(in.getVentanaSegundos());
        r.setTipo(tipoUp);
        if ("bands".equals(tipoUp)) {
            java.util.Map<String,Object> cfg = new java.util.HashMap<>();
            cfg.put("metric", "avg");
            if (in.getBandsHighValue()!=null) cfg.put("high", java.util.Map.of("op", ">", "value", in.getBandsHighValue(), "severity", java.util.Objects.toString(in.getBandsHighSeverity(), "grave")));
            if (in.getBandsLowValue()!=null) cfg.put("low", java.util.Map.of("op", "<", "value", in.getBandsLowValue(), "severity", java.util.Objects.toString(in.getBandsLowSeverity(), "bajo")));
            java.util.Map<String,Object> normal = new java.util.HashMap<>();
            if (in.getBandsNormalMin()!=null) normal.put("min", in.getBandsNormalMin());
            if (in.getBandsNormalMax()!=null) normal.put("max", in.getBandsNormalMax());
            if (!normal.isEmpty()) cfg.put("normal", normal);
            r.setConfig(cfg);
        } else {
            r.setConfig(java.util.Map.of());
        }
        return reglaRepo.save(r);
    }

    @Override
    @Transactional
    public void eliminar(UUID id) {
        reglaRepo.deleteById(id);
    }

    @Override
    public List<Regla> listarTodas() {
        return reglaRepo.findAllConVariable(); // ya viene con variable cargada
    }

    @Override
    public List<Regla> listarHabilitadas() {
        return reglaRepo.findByHabilitadaTrueConVariable(); // para el job si quieres evitar lazy
    }

    @Scheduled(fixedDelayString = "${app.rules.fixedDelay:60000}")
    @Override
    @Transactional
    public void evaluarReglas() {
        List<Regla> activas = reglaRepo.findByHabilitadaTrue();
        if (activas.isEmpty()) return;

        for (Regla r : activas) {
            try {
                int ventanaSeg = (r.getVentanaSegundos() == null ? 300 : r.getVentanaSegundos());
                OffsetDateTime since = OffsetDateTime.now().minusSeconds(ventanaSeg);
                List<Dispositivo> dispositivos;
                if (r.getDispositivo()!=null) {
                    dispositivos = java.util.List.of(r.getDispositivo());
                } else {
                    UUID plantillaId = r.getVariable().getPlantilla().getId();
                    dispositivos = dispositivoRepo.findByPlantilla_Id(plantillaId);
                }

                if ("bands".equalsIgnoreCase(r.getTipo())) {
                    for (Dispositivo d : dispositivos) {
                        TelemetriaRepository.AggRow agg = teleRepo.agg(d.getId(), r.getVariable().getId(), since);
                        if (agg == null || agg.getCnt() == null || agg.getCnt() == 0) continue;
                        Double valor = agg.getAvg();
                        if (valor == null) continue;
                        String sevCalc = evaluarBandas(r.getConfig(), valor);
                        if (sevCalc == null || "normal".equalsIgnoreCase(sevCalc)) continue; // no alertas para normal
                        Map<String, Object> det = new LinkedHashMap<>();
                        det.put("metric", "avg");
                        det.put("valor", valor);
                        det.put("ventanaSegundos", ventanaSeg);
                        det.put("severityCalculated", sevCalc);
                        det.put("variableId", r.getVariable().getId());
                        det.put("dispositivoId", d.getId());
                        det.put("reglaId", r.getId());
                        alertaService.crear(d, r, det);
                    }
                } else {
                    EvalSpec spec = parseExpresion(r);
                    for (Dispositivo d : dispositivos) {
                        TelemetriaRepository.AggRow agg = teleRepo.agg(d.getId(), r.getVariable().getId(), since);
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
                }
            } catch (Exception e) {
                log.error("Error evaluando regla {}: {}", r.getId(), e.getMessage(), e);
            }
        }
    }

    @Override
    @Transactional
    public void evaluarPara(UUID dispositivoId, UUID variableId) {
        if (dispositivoId == null || variableId == null) return;
        try { entityManager.flush(); } catch (Exception ignored) {}
        List<Regla> reglas = reglaRepo.findByHabilitadaTrueAndVariable_Id(variableId);
        if (reglas.isEmpty()) return;
        for (Regla r : reglas) {
            try {
                // Respeta el filtro por dispositivo en la regla (si lo tiene)
                if (r.getDispositivo() != null && !Objects.equals(r.getDispositivo().getId(), dispositivoId)) continue;
                int ventanaSeg = (r.getVentanaSegundos() == null ? 300 : r.getVentanaSegundos());
                OffsetDateTime since = OffsetDateTime.now().minusSeconds(ventanaSeg);
                TelemetriaRepository.AggRow agg = teleRepo.agg(dispositivoId, variableId, since);
                if (agg == null || agg.getCnt() == null || agg.getCnt() == 0) continue;
                if ("bands".equalsIgnoreCase(r.getTipo())) {
                    Double valor = agg.getAvg();
                    if (valor == null) continue;
                    String sevCalc = evaluarBandas(r.getConfig(), valor);
                    if (sevCalc == null || "normal".equalsIgnoreCase(sevCalc)) continue;
                    Map<String, Object> det = new LinkedHashMap<>();
                    det.put("metric", "avg");
                    det.put("valor", valor);
                    det.put("ventanaSegundos", ventanaSeg);
                    det.put("severityCalculated", sevCalc);
                    det.put("variableId", r.getVariable().getId());
                    det.put("dispositivoId", dispositivoId);
                    det.put("reglaId", r.getId());
                    var d = dispositivoRepo.findById(dispositivoId).orElse(null);
                    if (d != null) alertaService.crear(d, r, det);
                } else {
                    EvalSpec spec = parseExpresion(r);
                    Double valor = switch (spec.metric) {
                        case "avg" -> agg.getAvg();
                        case "min" -> agg.getMin();
                        case "max" -> agg.getMax();
                        default -> null;
                    };
                    if (valor == null) continue;
                    boolean cumple = comparar(valor, spec.op, spec.umbral);
                    if (!cumple) continue;
                    Map<String, Object> det = new LinkedHashMap<>();
                    det.put("metric", spec.metric);
                    det.put("valor", valor);
                    det.put("umbral", spec.umbral);
                    det.put("operador", spec.op);
                    det.put("ventanaSegundos", ventanaSeg);
                    det.put("variableId", r.getVariable().getId());
                    det.put("dispositivoId", dispositivoId);
                    det.put("reglaId", r.getId());
                    var d = dispositivoRepo.findById(dispositivoId).orElse(null);
                    if (d != null) alertaService.crear(d, r, det);
                }
            } catch (Exception e) {
                log.warn("Error evaluando regla inline {}: {}", r.getId(), e.toString());
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

    private String evaluarBandas(Map<String,Object> cfg, Double valor) {
        if (cfg == null) return null;
        try {
            Object high = cfg.get("high");
            if (high instanceof Map<?,?> h) {
                Object opObj = h.get("op");
                String op = (opObj != null) ? String.valueOf(opObj) : ">";
                double thr = Double.parseDouble(String.valueOf(h.get("value")));
                if (comparar(valor, op, thr)) {
                    Object sev = h.get("severity");
                    return sev != null ? String.valueOf(sev) : "grave";
                }
            }
            Object low = cfg.get("low");
            if (low instanceof Map<?,?> l) {
                Object opObjL = l.get("op");
                String op = (opObjL != null) ? String.valueOf(opObjL) : "<";
                double thr = Double.parseDouble(String.valueOf(l.get("value")));
                if (comparar(valor, op, thr)) {
                    Object sev = l.get("severity");
                    return sev != null ? String.valueOf(sev) : "bajo";
                }
            }
            Object normal = cfg.get("normal");
            if (normal instanceof Map<?,?> n) {
                Double min = n.get("min")!=null? Double.parseDouble(String.valueOf(n.get("min"))) : null;
                Double max = n.get("max")!=null? Double.parseDouble(String.valueOf(n.get("max"))) : null;
                boolean okMin = (min==null) || valor >= min;
                boolean okMax = (max==null) || valor <= max;
                if (okMin && okMax) return "normal";
            }
        } catch (Exception ignored) {}
        return null;
    }
}
