package com.unab.parcial2_iot.services.impl;

import com.unab.parcial2_iot.dto.EstadoDispositivoOut;
import com.unab.parcial2_iot.models.EstadoDispositivo;
import com.unab.parcial2_iot.repositories.EstadoDispositivoRepository;
import com.unab.parcial2_iot.repositories.ReglaRepository;
import com.unab.parcial2_iot.services.EstadoDispositivoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EstadoDispositivoServiceImpl implements EstadoDispositivoService {

    private final EstadoDispositivoRepository repo;
    private final ReglaRepository reglaRepository;

    @Override
    @Transactional(readOnly = true)
    public List<EstadoDispositivoOut> listarTodos() {
        var reglasActivas = new java.util.HashSet<java.util.UUID>(
                reglaVariablesActivas()
        );
        return repo.findAllWithJoins().stream()
                .map(e -> toDto(e, reglasActivas))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<EstadoDispositivoOut> listarPorDispositivo(UUID dispositivoId) {
        var reglasActivas = new java.util.HashSet<java.util.UUID>(
                reglaVariablesActivas()
        );
        return repo.findByDispositivoIdWithJoins(dispositivoId).stream()
                .map(e -> toDto(e, reglasActivas))
                .toList();
    }

    private EstadoDispositivoOut toDto(EstadoDispositivo e, java.util.Set<java.util.UUID> reglasActivas) {
        return EstadoDispositivoOut.builder()
                .dispositivoId(e.getDispositivo().getId())
                .dispositivoNombre(e.getDispositivo().getNombre())
                .variableId(e.getVariable().getId())
                .variableNombre(e.getVariable().getNombre())
                .variableEtiqueta(e.getVariable().getEtiqueta())
                .ultimoTs(e.getUltimoTs())
                .ultimoNumero(e.getUltimoNumero())
                .ultimoBooleano(e.getUltimoBooleano())
                .ultimoTexto(e.getUltimoTexto())
                .ultimoJson(e.getUltimoJson())
                .tieneRegla(reglasActivas.contains(e.getVariable().getId()))
                .build();
    }

    private java.util.List<java.util.UUID> reglaVariablesActivas() {
        // cargar variables con regla habilitada
        return reglaRepository.findByHabilitadaTrueConVariable().stream()
                .map(r -> r.getVariable().getId())
                .toList();
    }
}
