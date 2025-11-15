package com.unab.parcial2_iot.controller;

import com.unab.parcial2_iot.dto.TelemetriaIn;
import com.unab.parcial2_iot.services.TelemetriaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/telemetria")
public class TelemetriaRestController {

    private final TelemetriaService telemetriaService;
    private final com.unab.parcial2_iot.repositories.TelemetriaRepository teleRepo;
    private final com.unab.parcial2_iot.repositories.VariablePlantillaRepository varRepo;

    @PostMapping
    public ResponseEntity<?> registrar(@RequestBody TelemetriaIn in) {
        telemetriaService.registrarLectura(in);
        return ResponseEntity.accepted().build(); // 202 Accepted
    }

    @GetMapping("/series")
    public com.unab.parcial2_iot.dto.NumericSeries series(@RequestParam java.util.UUID dispositivoId,
                                                          @RequestParam java.util.UUID variableId,
                                                          @RequestParam(defaultValue = "24h") String range,
                                                          @RequestParam(defaultValue = "500") int max) {
        String r = range == null ? "24h" : range.toLowerCase();
        java.time.OffsetDateTime since = switch (r) {
            case "6h" -> java.time.OffsetDateTime.now().minusHours(6);
            case "7d" -> java.time.OffsetDateTime.now().minusDays(7);
            default -> java.time.OffsetDateTime.now().minusHours(24);
        };
        var vp = varRepo.findById(variableId).orElseThrow();
        int limit = Math.max(1, Math.min(max, 2000));
        var labels = new java.util.ArrayList<String>();
        var data = new java.util.ArrayList<Double>();
        var fmt = java.time.format.DateTimeFormatter.ofPattern("MM-dd HH:mm");
        if (vp.getTipoDato() == com.unab.parcial2_iot.models.TipoDato.booleano) {
            var rows = teleRepo.seriesBoolean(dispositivoId, variableId, since, limit);
            for (var r0 : rows) {
                var ts = java.time.OffsetDateTime.ofInstant(r0.getTs(), java.time.ZoneId.systemDefault());
                labels.add(ts.format(fmt));
                data.add(Boolean.TRUE.equals(r0.getValorBooleano()) ? 1.0 : 0.0);
            }
        } else if (vp.getTipoDato() == com.unab.parcial2_iot.models.TipoDato.numero) {
            var rows = teleRepo.seriesNumero(dispositivoId, variableId, since, limit);
            for (var r0 : rows) {
                var ts = java.time.OffsetDateTime.ofInstant(r0.getTs(), java.time.ZoneId.systemDefault());
                labels.add(ts.format(fmt));
                data.add(r0.getValorNumero());
            }
        } else {
            // por ahora, no graficamos textos/json; devolver vacío
        }
        return new com.unab.parcial2_iot.dto.NumericSeries(labels, data);
    }
}
