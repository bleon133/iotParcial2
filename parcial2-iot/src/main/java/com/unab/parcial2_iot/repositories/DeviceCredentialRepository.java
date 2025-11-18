package com.unab.parcial2_iot.repositories;

import com.unab.parcial2_iot.models.DeviceCredential;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface DeviceCredentialRepository extends JpaRepository<DeviceCredential, UUID> {
    Optional<DeviceCredential> findByDispositivo_Id(UUID dispositivoId);
}

