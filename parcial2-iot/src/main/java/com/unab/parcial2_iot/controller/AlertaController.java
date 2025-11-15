package com.unab.parcial2_iot.controller;

import com.unab.parcial2_iot.dto.AlertaOut;
import com.unab.parcial2_iot.repositories.DispositivoRepository;
import com.unab.parcial2_iot.repositories.ReglaRepository;
import com.unab.parcial2_iot.services.AlertaService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Controller
@RequiredArgsConstructor
@RequestMapping("/alertas")
public class AlertaController {

    private final AlertaService alertaService;
    private final DispositivoRepository dispositivoRepo;
    private final ReglaRepository reglaRepo;
    private final com.unab.parcial2_iot.repositories.AlertaRepository alertaRepo;

    @GetMapping
    public String listar(@RequestParam(required = false) String filtroTipo, // "regla"|"dispositivo"
                         @RequestParam(required = false, name = "id") String idStr,
                         @RequestParam(required = false) String severidad,
                         @RequestParam(defaultValue = "0") int page,
                         @RequestParam(defaultValue = "50") int size,
                         Model model) {

        var pageable = PageRequest.of(Math.max(0, page), Math.min(Math.max(size, 1), 200));
        // Normalizar id por si llega como texto "null"
        java.util.UUID id = null;
        if (idStr != null && !idStr.isBlank() && !"null".equalsIgnoreCase(idStr.trim())) {
            try { id = java.util.UUID.fromString(idStr.trim()); } catch (Exception ignored) {}
        }
        var pageDto = alertaService.listar(filtroTipo, id, severidad, pageable);

        model.addAttribute("page", pageDto);
        model.addAttribute("alertas", pageDto.getContent());
        model.addAttribute("dispositivos", dispositivoRepo.findAll());
        model.addAttribute("reglas", reglaRepo.findAll());
        model.addAttribute("filtroTipo", filtroTipo);
        model.addAttribute("filtroId", id);
        model.addAttribute("severidad", severidad);
        try { model.addAttribute("severidades", reglaRepo.findDistinctSeveridades()); } catch (Exception ignored) {}
        model.addAttribute("nav", "alertas");
        return "alertas/listar";
    }

    @GetMapping(value = "/export", produces = "text/csv")
    @ResponseBody
    public String exportCsv(@RequestParam(required = false) String filtroTipo,
                            @RequestParam(required = false, name = "id") String idStr,
                            @RequestParam(required = false) String severidad,
                            @RequestParam(required = false) java.time.OffsetDateTime since,
                            @RequestParam(required = false) java.time.OffsetDateTime until,
                            @RequestParam(defaultValue = "10000") int max) {
        var sb = new StringBuilder();
        sb.append("id,ts,reglaId,reglaNombre,dispositivoId,dispositivoNombre,detalles\n");
        var pageable = org.springframework.data.domain.PageRequest.of(0, Math.max(1, Math.min(max, 100000)));
        if (since == null) since = java.time.OffsetDateTime.now().minusHours(24);
        if (until == null) until = java.time.OffsetDateTime.now();

        // Normalizar parametros que pueden venir como "null"
        UUID id = null;
        if (idStr != null && !idStr.isBlank() && !"null".equalsIgnoreCase(idStr.trim())) {
            try { id = UUID.fromString(idStr.trim()); } catch (Exception ignored) {}
        }
        String sev = (severidad != null && !severidad.isBlank() && !"null".equalsIgnoreCase(severidad)) ? severidad : null;

        org.springframework.data.domain.Page<com.unab.parcial2_iot.models.Alerta> page;
        if (sev != null) {
            if ("regla".equalsIgnoreCase(filtroTipo) && id != null) {
                page = alertaRepo.findByRegla_IdAndRegla_SeveridadAndTsBetweenOrderByTsDesc(id, sev, since, until, pageable);
            } else if ("dispositivo".equalsIgnoreCase(filtroTipo) && id != null) {
                page = alertaRepo.findByDispositivo_IdAndRegla_SeveridadAndTsBetweenOrderByTsDesc(id, sev, since, until, pageable);
            } else {
                page = alertaRepo.findByRegla_SeveridadAndTsBetweenOrderByTsDesc(sev, since, until, pageable);
            }
        } else {
            if ("regla".equalsIgnoreCase(filtroTipo) && id != null) {
                page = alertaRepo.findByRegla_IdAndTsBetweenOrderByTsDesc(id, since, until, pageable);
            } else if ("dispositivo".equalsIgnoreCase(filtroTipo) && id != null) {
                page = alertaRepo.findByDispositivo_IdAndTsBetweenOrderByTsDesc(id, since, until, pageable);
            } else {
                page = alertaRepo.findByTsBetweenOrderByTsDesc(since, until, pageable);
            }
        }
        var mapper = new com.fasterxml.jackson.databind.ObjectMapper();
        for (var a : page.getContent()) {
            String detalles;
            try { detalles = mapper.writeValueAsString(a.getDetalles()); } catch (Exception e) { detalles = "{}"; }
            sb.append(a.getId()).append(',')
              .append(a.getTs()).append(',')
              .append(a.getRegla().getId()).append(',')
              .append(csv(a.getRegla().getNombre())).append(',')
              .append(a.getDispositivo().getId()).append(',')
              .append(csv(a.getDispositivo().getNombre())).append(',')
              .append('"').append(detalles.replace("\"", "''")).append('"')
              .append("\n");
        }
        return sb.toString();
    }

    private static String csv(String s) { if (s==null) return ""; return '"' + s.replace("\"","''") + '"'; }
}
