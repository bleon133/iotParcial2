package com.unab.parcial2_iot.repositories;

import com.unab.parcial2_iot.models.ComandoDispositivo;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;


public interface ComandoDispositivoRepository extends JpaRepository<ComandoDispositivo, UUID> {

    // Historial por dispositivo (incluye el dispositivo para evitar LazyInitializationException)
    @EntityGraph(attributePaths = "dispositivo")
    List<ComandoDispositivo> findByDispositivoIdOrderBySolicitadoEnDesc(UUID dispositivoId);

    // Historial global (también incluye dispositivo)
    @EntityGraph(attributePaths = "dispositivo")
    List<ComandoDispositivo> findAllByOrderBySolicitadoEnDesc();
}