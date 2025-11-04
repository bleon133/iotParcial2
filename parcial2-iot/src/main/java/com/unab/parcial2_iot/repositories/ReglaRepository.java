package com.unab.parcial2_iot.repositories;

import com.unab.parcial2_iot.models.Regla;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ReglaRepository extends JpaRepository<Regla, UUID> {
    List<Regla> findByHabilitadaTrue();
    List<Regla> findByVariablePlantilla_Id(UUID variableId);
}
