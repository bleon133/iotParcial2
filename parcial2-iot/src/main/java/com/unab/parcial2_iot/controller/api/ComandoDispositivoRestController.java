package com.unab.parcial2_iot.controller.api;

import com.unab.parcial2_iot.models.ComandoDispositivo;
import com.unab.parcial2_iot.models.EstadoComando;
import com.unab.parcial2_iot.services.ComandoDispositivoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/comandos")
@RequiredArgsConstructor
public class ComandoDispositivoRestController {

    private final ComandoDispositivoService comandoService;

    @PostMapping
    public ComandoDispositivo crear(@RequestParam UUID dispositivoId,
                                    @RequestParam String comando,
                                    @RequestParam(required = false) String datosJson,
                                    @RequestParam(required = false, defaultValue = "web") String solicitadoPor) {
        return comandoService.crear(dispositivoId, comando, datosJson, solicitadoPor);
    }

    @GetMapping("/dispositivo/{dispositivoId}")
    public List<ComandoDispositivo> historial(@PathVariable UUID dispositivoId) {
        return comandoService.historialPorDispositivo(dispositivoId);
    }

    @PatchMapping("/{id}/estado")
    public ComandoDispositivo actualizarEstado(@PathVariable UUID id,
                                               @RequestParam EstadoComando estado,
                                               @RequestParam(required = false) String mensajeError) {
        return comandoService.cambiarEstado(id, estado, mensajeError);
    }
}