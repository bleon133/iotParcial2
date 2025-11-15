package com.unab.parcial2_iot.repositories;

import com.unab.parcial2_iot.models.Regla;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface ReglaRepository extends JpaRepository<Regla, UUID> {

    // Para la pantalla de listado
    @Query("SELECT r FROM Regla r JOIN FETCH r.variable")
    List<Regla> findAllConVariable();

    @EntityGraph(attributePaths = "variable")
    List<Regla> findAll();   // para la vista

    @EntityGraph(attributePaths = "variable")
    List<Regla> findByHabilitadaTrue();  // para el scheduler

    @Query("SELECT r FROM Regla r JOIN FETCH r.variable WHERE r.habilitada = true")
    List<Regla> findByHabilitadaTrueConVariable();

    @Query("select distinct r.severidad from Regla r where r.severidad is not null order by r.severidad")
    List<String> findDistinctSeveridades();
}
