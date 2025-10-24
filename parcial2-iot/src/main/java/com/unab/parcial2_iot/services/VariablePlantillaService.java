package com.unab.parcial2_iot.services;

import com.unab.parcial2_iot.models.VariablePlantilla;

import java.util.List;
import java.util.UUID;

public interface VariablePlantillaService {

    List<VariablePlantilla> listarPorPlantilla(UUID plantillaId);

    VariablePlantilla crearParaPlantilla(UUID plantillaId, VariablePlantilla nueva);

    void eliminar(UUID plantillaId, UUID variableId);
}