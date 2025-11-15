package com.unab.parcial2_iot.controller.api;

import com.unab.parcial2_iot.models.Dispositivo;
import com.unab.parcial2_iot.models.VariablePlantilla;
import com.unab.parcial2_iot.repositories.DispositivoRepository;
import com.unab.parcial2_iot.repositories.VariablePlantillaRepository;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/dispositivos")
@RequiredArgsConstructor
public class DispositivoRestController {

    private final DispositivoRepository dispRepo;
    private final VariablePlantillaRepository varRepo;

    @GetMapping("/{id}/variables")
    public List<VarOption> variables(@PathVariable UUID id) {
        Dispositivo d = dispRepo.findById(id).orElseThrow();
        List<VariablePlantilla> vars = varRepo.findByPlantilla_IdOrderByNombreAsc(d.getPlantilla().getId());
        return vars.stream().map(v -> new VarOption(v.getId(), v.getNombre(), v.getEtiqueta())).toList();
    }

    @Data @AllArgsConstructor
    public static class VarOption { private UUID id; private String nombre; private String etiqueta; }
}

