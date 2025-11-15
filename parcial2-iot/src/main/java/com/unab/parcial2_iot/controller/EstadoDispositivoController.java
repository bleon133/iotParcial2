package com.unab.parcial2_iot.controller;

import com.unab.parcial2_iot.services.EstadoDispositivoService;
import com.unab.parcial2_iot.repositories.DispositivoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Controller
@RequiredArgsConstructor
@RequestMapping("/estado")
public class EstadoDispositivoController {

    private final EstadoDispositivoService estadoService;
    private final DispositivoRepository dispositivoRepo;

    @GetMapping
    public String listar(@RequestParam(name = "dispositivoId", required = false) String dispositivoIdStr,
                         Model model) {
        UUID dispositivoId = null;
        if (dispositivoIdStr != null && !dispositivoIdStr.isBlank() && !"null".equalsIgnoreCase(dispositivoIdStr)) {
            try { dispositivoId = UUID.fromString(dispositivoIdStr.trim()); } catch (Exception ignored) {}
        }

        model.addAttribute("dispositivos", dispositivoRepo.findAll());
        model.addAttribute("seleccionadoId", dispositivoId);
        model.addAttribute("nav", "estado");

        if (dispositivoId != null) {
            model.addAttribute("estados", estadoService.listarPorDispositivo(dispositivoId));
        } else {
            model.addAttribute("estados", estadoService.listarTodos());
        }

        return "estado/listar";
    }
}
