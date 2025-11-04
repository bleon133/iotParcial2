package com.unab.parcial2_iot.controller;

import com.unab.parcial2_iot.dto.ReglaIn;
import com.unab.parcial2_iot.models.Regla;
import com.unab.parcial2_iot.repositories.VariablePlantillaRepository;
import com.unab.parcial2_iot.services.ReglaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Controller
@RequiredArgsConstructor
@RequestMapping("/reglas")
public class ReglaController {

    private final ReglaService reglaService;
    private final VariablePlantillaRepository varRepo;

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("reglas", reglaService.listarTodas());
        model.addAttribute("variables", varRepo.findAll());
        model.addAttribute("form", new ReglaIn());
        return "reglas/listar";
    }

    @PostMapping
    public String crear(@ModelAttribute("form") @Valid ReglaIn in) {
        reglaService.crear(in);
        return "redirect:/reglas";
    }

    @PostMapping("/{id}")
    public String actualizar(@PathVariable UUID id, @ModelAttribute("form") @Valid ReglaIn in) {
        reglaService.actualizar(id, in);
        return "redirect:/reglas";
    }

    @PostMapping("/{id}/toggle")
    public String toggle(@PathVariable UUID id) {
        Regla r = reglaService.listarTodas().stream()
                .filter(x -> x.getId().equals(id)).findFirst()
                .orElseThrow();

        var in = new ReglaIn();
        in.setVariablePlantillaId(r.getVariable().getId());      // <-- variable
        in.setNombre(r.getNombre());
        in.setExpresion(r.getExpresion());
        in.setSeveridad(r.getSeveridad());
        in.setHabilitada(!r.isHabilitada());                     // <-- isHabilitada()
        in.setVentanaSegundos(r.getVentanaSegundos());

        reglaService.actualizar(id, in);
        return "redirect:/reglas";
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable UUID id) {
        reglaService.eliminar(id);
        return "redirect:/reglas";
    }
}
