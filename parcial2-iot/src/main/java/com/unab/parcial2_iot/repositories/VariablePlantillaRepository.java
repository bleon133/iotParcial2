package com.unab.parcial2_iot.repositories;

import com.unab.parcial2_iot.models.VariablePlantilla;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface VariablePlantillaRepository extends JpaRepository<VariablePlantilla, UUID> {

    List<VariablePlantilla> findByPlantilla_IdOrderByNombreAsc(UUID plantillaId);

    boolean existsByPlantilla_IdAndNombreIgnoreCase(UUID plantillaId, String nombre);
}