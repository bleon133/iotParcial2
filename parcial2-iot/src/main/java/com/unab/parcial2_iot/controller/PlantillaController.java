package com.unab.parcial2_iot.controller;

import com.unab.parcial2_iot.models.Plantilla;
import com.unab.parcial2_iot.models.Protocolo;
import com.unab.parcial2_iot.services.PlantillaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/plantillas")
public class PlantillaController {

    private final PlantillaService plantillaService;

    @GetMapping
    public String vistaCrear(Model model,
                             @RequestParam(value = "ok", required = false) String ok,
                             @RequestParam(value = "error", required = false) String error) {
        model.addAttribute("plantilla", new Plantilla()); // objeto del formulario
        model.addAttribute("protocolos", Protocolo.values());
        model.addAttribute("plantillas", plantillaService.listar());
        model.addAttribute("titulo", "Plantillas");
        model.addAttribute("ok", ok);
        model.addAttribute("error", error);
        return "Plantillas/crear"; // templates/plantillas/crear.html
    }

    @PostMapping
    public String crear(@ModelAttribute("plantilla") Plantilla form,
                        BindingResult binding,
                        Model model) {
        try {
            // Validación mínima
            if (form.getNombre() == null || form.getNombre().trim().isEmpty()) {
                binding.rejectValue("nombre", "nombre.obligatorio", "El nombre es obligatorio");
            }
            if (form.getProtocoloPredeterminado() == null) {
                binding.rejectValue("protocoloPredeterminado", "proto.obligatorio", "Selecciona un protocolo");
            }

            if (binding.hasErrors()) {
                model.addAttribute("protocolos", Protocolo.values());
                model.addAttribute("plantillas", plantillaService.listar());
                return "plantillas/crear";
            }

            plantillaService.crear(form);
            return "redirect:/plantillas?ok=Plantilla%20creada";
        } catch (IllegalStateException dup) {
            binding.rejectValue("nombre", "nombre.duplicado", dup.getMessage());
        } catch (IllegalArgumentException iae) {
            binding.reject("form.invalido", iae.getMessage());
        } catch (Exception ex) {
            binding.reject("form.error", "Error inesperado: " + ex.getMessage());
        }

        model.addAttribute("protocolos", Protocolo.values());
        model.addAttribute("plantillas", plantillaService.listar());
        return "Plantillas/crear";
    }
}