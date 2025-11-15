package com.unab.parcial2_iot.controller.api;

import com.unab.parcial2_iot.models.VariablePlantilla;
import com.unab.parcial2_iot.repositories.VariablePlantillaRepository;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.UUID;

@RestController
@RequestMapping("/api/variables")
@RequiredArgsConstructor
public class VariableRestController {

    private final VariablePlantillaRepository varRepo;

    @GetMapping("/{id}/meta")
    public Meta meta(@PathVariable UUID id) {
        VariablePlantilla v = varRepo.findById(id).orElseThrow();
        return new Meta(v.getNombre(), v.getEtiqueta(), v.getUnidad(),
                v.getTipoDato().name(), v.getMinimo(), v.getMaximo(), v.getPrecisionDecimales());
    }

    @Data @AllArgsConstructor
    public static class Meta {
        private String nombre;
        private String etiqueta;
        private String unidad;
        private String tipo;
        private BigDecimal minimo;
        private BigDecimal maximo;
        private Integer precision;
    }
}

