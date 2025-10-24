package com.unab.parcial2_iot.services.impl;

import com.unab.parcial2_iot.models.Plantilla;
import com.unab.parcial2_iot.models.TipoDato;
import com.unab.parcial2_iot.models.VariablePlantilla;
import com.unab.parcial2_iot.repositories.PlantillaRepository;
import com.unab.parcial2_iot.repositories.VariablePlantillaRepository;
import com.unab.parcial2_iot.services.VariablePlantillaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VariablePlantillaServiceImpl implements VariablePlantillaService {

    private final VariablePlantillaRepository variableRepo;
    private final PlantillaRepository plantillaRepo;

    @Override
    public List<VariablePlantilla> listarPorPlantilla(UUID plantillaId) {
        return variableRepo.findByPlantilla_IdOrderByNombreAsc(plantillaId);
    }

    @Override
    @Transactional
    public VariablePlantilla crearParaPlantilla(UUID plantillaId, VariablePlantilla nueva) {
        Plantilla plantilla = plantillaRepo.findById(plantillaId)
                .orElseThrow(() -> new IllegalArgumentException("Plantilla no encontrada"));

        if (nueva == null) throw new IllegalArgumentException("Variable requerida");

        // Validaciones mínimas
        if (nueva.getNombre() == null || nueva.getNombre().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre es obligatorio");
        }
        if (nueva.getTipoDato() == null) {
            throw new IllegalArgumentException("El tipo de dato es obligatorio");
        }

        // Unicidad por (plantilla, nombre)
        final String nombre = nueva.getNombre().trim();
        if (variableRepo.existsByPlantilla_IdAndNombreIgnoreCase(plantillaId, nombre)) {
            throw new IllegalStateException("Ya existe una variable con ese nombre en esta plantilla");
        }

        // Reglas de consistencia simples
        BigDecimal min = nueva.getMinimo();
        BigDecimal max = nueva.getMaximo();
        if (min != null && max != null && min.compareTo(max) > 0) {
            throw new IllegalArgumentException("El mínimo no puede ser mayor que el máximo");
        }

        if (nueva.getPrecisionDecimales() != null && nueva.getPrecisionDecimales() < 0) {
            throw new IllegalArgumentException("La precisión no puede ser negativa");
        }

        if (nueva.getMuestreoMs() != null && nueva.getMuestreoMs() < 0) {
            throw new IllegalArgumentException("El muestreo (ms) no puede ser negativo");
        }

        // Sugerencia: para tipo json/booleano ignorar min/max/precision
        if (nueva.getTipoDato() == TipoDato.json || nueva.getTipoDato() == TipoDato.booleano) {
            nueva.setMinimo(null);
            nueva.setMaximo(null);
            nueva.setPrecisionDecimales(null);
        }

        nueva.setNombre(nombre);
        nueva.setPlantilla(plantilla);

        return variableRepo.save(nueva);
    }

    @Override
    @Transactional
    public void eliminar(UUID plantillaId, UUID variableId) {
        VariablePlantilla var = variableRepo.findById(variableId)
                .orElseThrow(() -> new IllegalArgumentException("Variable no encontrada"));
        if (!var.getPlantilla().getId().equals(plantillaId)) {
            throw new IllegalArgumentException("La variable no pertenece a la plantilla indicada");
        }
        variableRepo.delete(var);
    }
}