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

        if (dispositivoId != null) {
            // cargar variables de la plantilla del dispositivo
            var disp = dispositivoRepo.findById(dispositivoId).orElse(null);
            if (disp != null) {
                variables = varRepo.findByPlantilla_IdOrderByNombreAsc(disp.getPlantilla().getId());
            }
        }

        if (dispositivoId != null && variableId != null) {
            filas = teleRepo.ultimas(dispositivoId, variableId, Math.min(Math.max(limit, 1), 500));
        }

        model.addAttribute("dispositivos", dispositivos);
        model.addAttribute("variables", variables);
        model.addAttribute("filas", filas);
        model.addAttribute("dispositivoId", dispositivoId);
        model.addAttribute("variableId", variableId);
        model.addAttribute("limit", limit);

        return "telemetria/listar"; // templates/telemetria/listar.html
    }
}