package com.unab.parcial2_iot.repositories;

import com.unab.parcial2_iot.models.DeviceTwin;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface DeviceTwinRepository extends JpaRepository<DeviceTwin, UUID> {
    Optional<DeviceTwin> findByDispositivoId(UUID dispositivoId);
}

