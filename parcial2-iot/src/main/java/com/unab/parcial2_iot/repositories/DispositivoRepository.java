package com.unab.parcial2_iot.repositories;

import com.unab.parcial2_iot.models.Dispositivo;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DispositivoRepository extends JpaRepository<Dispositivo, UUID> {

    boolean existsByIdExternoIgnoreCase(String idExterno);

    @EntityGraph(attributePaths = {"plantilla"})
    List<Dispositivo> findAllByOrderByCreadoEnDesc();

    Optional<Dispositivo> findByIdExternoIgnoreCase(String idExterno);
}