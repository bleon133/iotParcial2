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

    long countByTsAfter(java.time.OffsetDateTime ts);

    interface HourCountRow { java.time.Instant getBucket(); Long getCnt(); }

    @org.springframework.data.jpa.repository.Query(value = """
            select date_trunc('hour', ts) as bucket, count(*) as cnt
            from iot.alerta
            where ts >= :since
            group by 1
            order by 1
            """, nativeQuery = true)
    java.util.List<HourCountRow> countByHourSince(java.time.OffsetDateTime since);

    interface DayCountRow { java.time.Instant getBucket(); Long getCnt(); }

    @org.springframework.data.jpa.repository.Query(value = """
            select date_trunc('day', ts) as bucket, count(*) as cnt
            from iot.alerta
            where ts >= :since
            group by 1
            order by 1
            """, nativeQuery = true)
    java.util.List<DayCountRow> countByDaySince(java.time.OffsetDateTime since);

    // Filtros por severidad
    Page<Alerta> findByRegla_SeveridadOrderByTsDesc(String severidad, Pageable pageable);
    Page<Alerta> findByDispositivo_IdAndRegla_SeveridadOrderByTsDesc(UUID dispositivoId, String severidad, Pageable pageable);
    Page<Alerta> findByRegla_IdAndRegla_SeveridadOrderByTsDesc(UUID reglaId, String severidad, Pageable pageable);

    // Rango temporal para export
    Page<Alerta> findByTsBetweenOrderByTsDesc(java.time.OffsetDateTime since, java.time.OffsetDateTime until, Pageable pageable);
    Page<Alerta> findByRegla_IdAndTsBetweenOrderByTsDesc(UUID reglaId, java.time.OffsetDateTime since, java.time.OffsetDateTime until, Pageable pageable);
    Page<Alerta> findByDispositivo_IdAndTsBetweenOrderByTsDesc(UUID dispId, java.time.OffsetDateTime since, java.time.OffsetDateTime until, Pageable pageable);
    Page<Alerta> findByRegla_SeveridadAndTsBetweenOrderByTsDesc(String severidad, java.time.OffsetDateTime since, java.time.OffsetDateTime until, Pageable pageable);
    Page<Alerta> findByRegla_IdAndRegla_SeveridadAndTsBetweenOrderByTsDesc(UUID reglaId, String severidad, java.time.OffsetDateTime since, java.time.OffsetDateTime until, Pageable pageable);
    Page<Alerta> findByDispositivo_IdAndRegla_SeveridadAndTsBetweenOrderByTsDesc(UUID dispId, String severidad, java.time.OffsetDateTime since, java.time.OffsetDateTime until, Pageable pageable);
}
