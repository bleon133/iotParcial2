package com.unab.parcial2_iot.repositories;

import com.unab.parcial2_iot.models.EstadoDispositivo;
import com.unab.parcial2_iot.models.EstadoDispositivoId;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface EstadoDispositivoRepository extends JpaRepository<EstadoDispositivo, EstadoDispositivoId> {

    @EntityGraph(attributePaths = {"variablePlantilla"})
    List<EstadoDispositivo> findByDispositivo_Id(UUID dispositivoId);
}