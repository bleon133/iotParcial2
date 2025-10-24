package com.unab.parcial2_iot.repositories;

import com.unab.parcial2_iot.models.Plantilla;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PlantillaRepository extends JpaRepository<Plantilla, UUID> {
    boolean existsByNombreIgnoreCase(String nombre);
    Optional<Plantilla> findByNombreIgnoreCase(String nombre);
}