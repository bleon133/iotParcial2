package com.unab.parcial2_iot.controller;

import com.unab.parcial2_iot.models.Dispositivo;
import com.unab.parcial2_iot.models.VariablePlantilla;
import com.unab.parcial2_iot.repositories.DispositivoRepository;
import com.unab.parcial2_iot.repositories.TelemetriaRepository;
import com.unab.parcial2_iot.repositories.VariablePlantillaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/telemetria")
public class TelemetriaController {

    private final DispositivoRepository dispositivoRepo;
    private final VariablePlantillaRepository varRepo;
    private final TelemetriaRepository teleRepo;

    @GetMapping
    public String vista(@RequestParam(required = false) UUID dispositivoId,
                        @RequestParam(required = false) UUID variableId,
                        @RequestParam(required = false, defaultValue = "50") int limit,
                        Model model) {

        List<Dispositivo> dispositivos = dispositivoRepo.findAll();
        List<VariablePlantilla> variables = Collections.emptyList();
        List<TelemetriaRepository.TelemetriaRow> filas = Collections.emptyList();

        String dispositivoNombre = null;
        String variableNombre = null;

        if (dispositivoId != null) {
            // buscar dispositivo seleccionado en la lista ya cargada
            Dispositivo dispSel = dispositivos.stream()
                    .filter(d -> d.getId().equals(dispositivoId))
                    .findFirst()
                    .orElse(null);

            if (dispSel != null) {
                dispositivoNombre = dispSel.getNombre();
                // cargar variables de su plantilla
                variables = varRepo.findByPlantilla_IdOrderByNombreAsc(dispSel.getPlantilla().getId());
            }
        }

        if (dispositivoId != null && variableId != null) {
            // nombre de variable (si está en la lista cargada)
            variableNombre = variables.stream()
                    .filter(v -> v.getId().equals(variableId))
                    .map(VariablePlantilla::getNombre)
                    .findFirst()
                    .orElse(null);

            filas = teleRepo.ultimas(dispositivoId, variableId, Math.min(Math.max(limit, 1), 500));
        }

        model.addAttribute("dispositivos", dispositivos);
        model.addAttribute("variables", variables);
        model.addAttribute("filas", filas);
        model.addAttribute("dispositivoId", dispositivoId);
        model.addAttribute("variableId", variableId);
        model.addAttribute("limit", limit);

        // ➕ para el header y el sidebar
        model.addAttribute("dispositivoNombre", dispositivoNombre);
        model.addAttribute("variableNombre", variableNombre);
        model.addAttribute("titulo", "Telemetría");
        model.addAttribute("nav", "telemetria");

        return "telemetria/listar";
    }

    @GetMapping(value = "/export", produces = "text/csv")
    @ResponseBody
    public String exportCsv(@RequestParam UUID dispositivoId,
                            @RequestParam UUID variableId,
                            @RequestParam(required = false) java.time.OffsetDateTime since,
                            @RequestParam(required = false) java.time.OffsetDateTime until) {
        if (since == null) since = java.time.OffsetDateTime.now().minusHours(24);
        if (until == null) until = java.time.OffsetDateTime.now();
        var rows = teleRepo.enRango(dispositivoId, variableId, since, until);
        var sb = new StringBuilder();
        sb.append("ts,valorNumero,valorBooleano,valorTexto,valorJson\n");
        for (var r : rows) {
            sb.append(r.getTs()).append(',')
              .append(r.getValorNumero() == null ? "" : r.getValorNumero()).append(',')
              .append(r.getValorBooleano() == null ? "" : r.getValorBooleano()).append(',')
              .append(csv(r.getValorTexto())).append(',')
              .append(csv(r.getValorJson()))
              .append("\n");
        }
        return sb.toString();
    }

    private static String csv(String s) { if (s==null) return ""; return '"' + s.replace("\"","''") + '"'; }
}
