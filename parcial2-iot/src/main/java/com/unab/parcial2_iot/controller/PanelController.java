package com.unab.parcial2_iot.controller;

import com.unab.parcial2_iot.models.Panel;
import com.unab.parcial2_iot.models.PanelWidget;
import com.unab.parcial2_iot.repositories.DispositivoRepository;
import com.unab.parcial2_iot.repositories.PanelRepository;
import com.unab.parcial2_iot.repositories.PanelWidgetRepository;
import com.unab.parcial2_iot.repositories.VariablePlantillaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
@RequestMapping("/paneles")
public class PanelController {

    private final PanelRepository panelRepo;
    private final PanelWidgetRepository widgetRepo;
    private final DispositivoRepository dispositivoRepo;
    private final VariablePlantillaRepository varRepo;
    private final com.unab.parcial2_iot.repositories.PanelWidgetSerieRepository serieRepo;

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("paneles", panelRepo.findAll());
        model.addAttribute("nuevo", new Panel());
        model.addAttribute("nav", "paneles");
        return "paneles/listar";
    }

    @PostMapping
    public String crear(@ModelAttribute("nuevo") Panel p) {
        p.setCreadoEn(OffsetDateTime.now());
        if (p.getNombre() == null || p.getNombre().isBlank()) p.setNombre("Panel");
        panelRepo.save(p);
        return "redirect:/paneles";
    }

    @GetMapping("/{id}")
    public String ver(@PathVariable UUID id, Model model) {
        Panel p = panelRepo.findById(id).orElseThrow();
        model.addAttribute("panel", p);
        model.addAttribute("widgets", widgetRepo.findByPanel_IdOrderByPosAsc(id));
        model.addAttribute("dispositivos", dispositivoRepo.findAll());
        model.addAttribute("nav", "paneles");
        return "paneles/ver";
    }

    @PostMapping("/{id}/widgets")
    public String addWidget(@PathVariable UUID id,
                            @RequestParam(required = false) UUID dispositivoId,
                            @RequestParam(required = false) UUID variableId,
                            @RequestParam(defaultValue = "line") String chartType,
                            @RequestParam(defaultValue = "24h") String rango,
                            @RequestParam(required = false) String titulo,
                            @RequestParam(required = false, defaultValue = "#0d6efd") String color) {
        if (dispositivoId == null || variableId == null) {
            // Faltan parámetros (p.ej., usuario no seleccionó variable). Volver a la vista del panel.
            return "redirect:/paneles/{id}";
        }
        var panel = panelRepo.findById(id).orElseThrow();
        var disp = dispositivoRepo.findById(dispositivoId).orElseThrow();
        var var = varRepo.findById(variableId).orElseThrow();
        // Título por defecto si viene vacío
        if (titulo == null || titulo.isBlank()) {
            String varLabel = (var.getEtiqueta() != null && !var.getEtiqueta().isBlank()) ? var.getEtiqueta() : var.getNombre();
            titulo = disp.getNombre() + " - " + varLabel;
        }
        PanelWidget w = PanelWidget.builder()
                .panel(panel)
                .dispositivo(disp)
                .variable(var)
                .chartType(chartType)
                .rango(rango)
                .titulo(titulo)
                .color(color)
                .pos(0)
                .build();
        widgetRepo.save(w);
        return "redirect:/paneles/{id}";
    }

    @PostMapping("/{panelId}/widgets/{widgetId}/eliminar")
    public String delWidget(@PathVariable UUID panelId, @PathVariable UUID widgetId) {
        widgetRepo.deleteById(widgetId);
        return "redirect:/paneles/{panelId}";
    }

    @PostMapping("/{panelId}/widgets/{widgetId}/series")
    public String addSerie(@PathVariable UUID panelId,
                           @PathVariable UUID widgetId,
                           @RequestParam(required = false) UUID variableId,
                           @RequestParam(required = false) String color,
                           @RequestParam(required = false) String label) {
        if (variableId == null) return "redirect:/paneles/{panelId}";
        var w = widgetRepo.findById(widgetId).orElseThrow();
        if (!w.getPanel().getId().equals(panelId)) return "redirect:/paneles/{panelId}";
        var var = varRepo.findById(variableId).orElseThrow();
        var s = com.unab.parcial2_iot.models.PanelWidgetSerie.builder()
                .widget(w)
                .variable(var)
                .color((color == null || color.isBlank()) ? null : color)
                .label((label == null || label.isBlank()) ? null : label)
                .build();
        serieRepo.save(s);
        return "redirect:/paneles/{panelId}";
    }

    @PostMapping("/{panelId}/widgets/{widgetId}/series/{serieId}/eliminar")
    public String delSerie(@PathVariable UUID panelId,
                           @PathVariable UUID widgetId,
                           @PathVariable UUID serieId) {
        var s = serieRepo.findById(serieId).orElse(null);
        if (s != null && s.getWidget().getId().equals(widgetId) && s.getWidget().getPanel().getId().equals(panelId)) {
            serieRepo.deleteById(serieId);
        }
        return "redirect:/paneles/{panelId}";
    }

    @PostMapping("/{id}/widgets/orden")
    @ResponseBody
    public void ordenar(@PathVariable UUID id, @RequestParam String order) {
        if (order == null || order.isBlank()) return;
        var ids = java.util.Arrays.stream(order.split(",")).map(UUID::fromString).toList();
        var list = widgetRepo.findByPanel_IdOrderByPosAsc(id);
        var pos = new java.util.HashMap<UUID,Integer>();
        for (int i=0;i<ids.size();i++) pos.put(ids.get(i), i);
        for (var w : list) {
            Integer p = pos.get(w.getId());
            if (p != null) w.setPos(p);
        }
        widgetRepo.saveAll(list);
    }

    @PostMapping("/{panelId}/widgets/{widgetId}/titulo")
    @ResponseBody
    public void actualizarTitulo(@PathVariable UUID panelId,
                                 @PathVariable UUID widgetId,
                                 @RequestParam String titulo) {
        var w = widgetRepo.findById(widgetId).orElseThrow();
        if (!w.getPanel().getId().equals(panelId)) return;
        w.setTitulo(titulo);
        widgetRepo.save(w);
    }

    @GetMapping("/{panelId}/widgets/{widgetId}/series")
    @ResponseBody
    public java.util.List<com.unab.parcial2_iot.repositories.PanelWidgetSerieRepository.SerieDTO> series(@PathVariable UUID panelId,
                                                                                                         @PathVariable UUID widgetId) {
        // no validamos panelId aquí para simplificar
        return serieRepo.findSeriesDto(widgetId);
    }
}
