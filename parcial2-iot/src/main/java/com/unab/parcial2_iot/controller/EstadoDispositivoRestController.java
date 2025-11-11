package com.unab.parcial2_iot.controller;

import com.unab.parcial2_iot.dto.EstadoDispositivoOut;
import com.unab.parcial2_iot.services.EstadoDispositivoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/estado")
public class EstadoDispositivoRestController {

    private final EstadoDispositivoService service;

    @GetMapping
    public List<EstadoDispositivoOut> listar(
            @RequestParam(name = "dispositivoId", required = false) UUID dispositivoId) {

        if (dispositivoId != null) {
            return service.listarPorDispositivo(dispositivoId);
        }
        return service.listarTodos();
    }
}