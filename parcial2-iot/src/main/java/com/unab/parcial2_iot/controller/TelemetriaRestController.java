package com.unab.parcial2_iot.controller;

import com.unab.parcial2_iot.dto.TelemetriaIn;
import com.unab.parcial2_iot.services.TelemetriaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/telemetria")
public class TelemetriaRestController {

    private final TelemetriaService telemetriaService;

    @PostMapping
    public ResponseEntity<?> registrar(@RequestBody TelemetriaIn in) {
        telemetriaService.registrarLectura(in);
        return ResponseEntity.accepted().build(); // 202 Accepted
    }
}