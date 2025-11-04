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

    @GetMapping
    public String listar(@RequestParam(required = false) String filtroTipo, // "regla"|"dispositivo"
                         @RequestParam(required = false) UUID id,
                         @RequestParam(defaultValue = "0") int page,
                         @RequestParam(defaultValue = "50") int size,
                         Model model) {

        var pageable = PageRequest.of(Math.max(0, page), Math.min(Math.max(size, 1), 200));
        var pageDto = alertaService.listar(filtroTipo, id, pageable);

        model.addAttribute("page", pageDto);
        model.addAttribute("alertas", pageDto.getContent());
        model.addAttribute("dispositivos", dispositivoRepo.findAll());
        model.addAttribute("reglas", reglaRepo.findAll());
        model.addAttribute("filtroTipo", filtroTipo);
        model.addAttribute("filtroId", id);
        return "alertas/listar";
    }
}
