package com.unab.parcial2_iot.repositories;


import com.unab.parcial2_iot.models.EstadoDispositivo;
import com.unab.parcial2_iot.models.EstadoDispositivoId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface EstadoDispositivoRepository extends JpaRepository<EstadoDispositivo, EstadoDispositivoId> {

    @Query("""
        SELECT e FROM EstadoDispositivo e
        JOIN FETCH e.dispositivo d
        JOIN FETCH e.variable v
        """)
    List<EstadoDispositivo> findAllWithJoins();

    @Query("""
        SELECT e FROM EstadoDispositivo e
        JOIN FETCH e.dispositivo d
        JOIN FETCH e.variable v
        WHERE d.id = :dispositivoId
        """)
    List<EstadoDispositivo> findByDispositivoIdWithJoins(@Param("dispositivoId") UUID dispositivoId);

    @Query("""
        SELECT e FROM EstadoDispositivo e
        JOIN FETCH e.dispositivo d
        JOIN FETCH e.variable v
        WHERE d.id = :dispositivoId AND v.id = :variableId
        """)
    java.util.Optional<EstadoDispositivo> findOneWithJoins(@Param("dispositivoId") UUID dispositivoId,
                                                           @Param("variableId") UUID variableId);
}
