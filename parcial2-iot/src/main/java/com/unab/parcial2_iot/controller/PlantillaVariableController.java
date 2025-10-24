package com.unab.parcial2_iot.controller;


import com.unab.parcial2_iot.models.Plantilla;
import com.unab.parcial2_iot.models.TipoDato;
import com.unab.parcial2_iot.models.VariablePlantilla;
import com.unab.parcial2_iot.repositories.PlantillaRepository;
import com.unab.parcial2_iot.services.VariablePlantillaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Controller
@RequiredArgsConstructor
@RequestMapping("/plantillas/{plantillaId}/variables")
public class PlantillaVariableController {

    private final VariablePlantillaService variableService;
    private final PlantillaRepository plantillaRepo;

    @ModelAttribute("plantilla")
    public Plantilla cargarPlantilla(@PathVariable("plantillaId") UUID plantillaId) {
        return plantillaRepo.findById(plantillaId)
                .orElseThrow(() -> new IllegalArgumentException("Plantilla no encontrada"));
    }

    @GetMapping
    public String vista(Model model,
                        @ModelAttribute("plantilla") Plantilla plantilla,
                        @RequestParam(value = "ok", required = false) String ok,
                        @RequestParam(value = "error", required = false) String error) {

        model.addAttribute("nuevaVariable", new VariablePlantilla());
        model.addAttribute("tipos", TipoDato.values());
        model.addAttribute("variables", variableService.listarPorPlantilla(plantilla.getId()));
        model.addAttribute("ok", ok);
        model.addAttribute("error", error);
        return "plantillas/variables"; // templates/plantillas/variables.html
    }

    @PostMapping
    public String crear(@PathVariable("plantillaId") UUID plantillaId,
                        @ModelAttribute("nuevaVariable") VariablePlantilla form,
                        BindingResult binding,
                        Model model,
                        @ModelAttribute("plantilla") Plantilla plantilla) {
        try {
            // Validación simple
            if (form.getNombre() == null || form.getNombre().trim().isEmpty()) {
                binding.rejectValue("nombre", "nombre.obligatorio", "El nombre es obligatorio");
            }
            if (form.getTipoDato() == null) {
                binding.rejectValue("tipoDato", "tipo.obligatorio", "Selecciona el tipo de dato");
            }
            if (binding.hasErrors()) {
                model.addAttribute("tipos", TipoDato.values());
                model.addAttribute("variables", variableService.listarPorPlantilla(plantillaId));
                return "plantillas/variables";
            }

            variableService.crearParaPlantilla(plantillaId, form);
            return "redirect:/plantillas/{plantillaId}/variables?ok=Variable%20creada";
        } catch (IllegalStateException dup) {
            binding.rejectValue("nombre", "nombre.duplicado", dup.getMessage());
        } catch (IllegalArgumentException iae) {
            binding.reject("form.invalido", iae.getMessage());
        } catch (Exception ex) {
            binding.reject("form.error", "Error inesperado: " + ex.getMessage());
        }

        model.addAttribute("tipos", TipoDato.values());
        model.addAttribute("variables", variableService.listarPorPlantilla(plantillaId));
        return "plantillas/variables";
    }

    @PostMapping("/{variableId}/eliminar")
    public String eliminar(@PathVariable("plantillaId") UUID plantillaId,
                           @PathVariable("variableId") UUID variableId) {
        variableService.eliminar(plantillaId, variableId);
        return "redirect:/plantillas/{plantillaId}/variables?ok=Variable%20eliminada";
    }
}