package com.unab.parcial2_iot.controller;

import com.unab.parcial2_iot.dto.NuevoComandoForm;
import com.unab.parcial2_iot.repositories.DispositivoRepository;
import com.unab.parcial2_iot.services.ComandoDispositivoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.UUID;

@Controller
@RequestMapping("/comandos")
@RequiredArgsConstructor
public class ComandoDispositivoController {

    private final DispositivoRepository dispositivoRepo;
    private final ComandoDispositivoService comandoService;

    @GetMapping
    public String listar(@RequestParam(value = "dispositivoId", required = false) UUID dispositivoId,
                         Model model) {

        model.addAttribute("titulo", "Comandos a dispositivos");
        model.addAttribute("nav", "comandos");
        model.addAttribute("dispositivos", dispositivoRepo.findAll());
        model.addAttribute("nuevoComando", new NuevoComandoForm());
        model.addAttribute("comandos", comandoService.historialPorDispositivo(dispositivoId));
        model.addAttribute("seleccionadoId", dispositivoId);

        return "comandos/listar";
    }

    @PostMapping
    public String crear(@ModelAttribute("nuevoComando") NuevoComandoForm form,
                        RedirectAttributes flash) {

        try {
            comandoService.crear(
                    form.getDispositivoId(),
                    form.getComando(),
                    form.getDatos(),
                    form.getSolicitadoPor()
            );
            flash.addFlashAttribute("ok", "Comando enviado correctamente.");
        } catch (Exception e) {
            flash.addFlashAttribute("error", "No se pudo enviar el comando: " + e.getMessage());
        }

        return "redirect:/comandos";
    }
}
