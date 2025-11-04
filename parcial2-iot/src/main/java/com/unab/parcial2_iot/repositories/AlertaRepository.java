package com.unab.parcial2_iot.repositories;

import com.unab.parcial2_iot.models.Alerta;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AlertaRepository extends JpaRepository<Alerta, UUID> {
    Page<Alerta> findByRegla_IdOrderByTsDesc(UUID reglaId, Pageable pageable);
    Page<Alerta> findByDispositivo_IdOrderByTsDesc(UUID dispositivoId, Pageable pageable);
    Page<Alerta> findAllByOrderByTsDesc(Pageable pageable);
}
