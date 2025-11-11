package com.unab.parcial2_iot.services.impl;

import com.unab.parcial2_iot.dto.EstadoDispositivoOut;
import com.unab.parcial2_iot.models.EstadoDispositivo;
import com.unab.parcial2_iot.repositories.EstadoDispositivoRepository;
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

    @Override
    @Transactional(readOnly = true)
    public List<EstadoDispositivoOut> listarTodos() {
        return repo.findAllWithJoins().stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<EstadoDispositivoOut> listarPorDispositivo(UUID dispositivoId) {
        return repo.findByDispositivoIdWithJoins(dispositivoId).stream()
                .map(this::toDto)
                .toList();
    }

    private EstadoDispositivoOut toDto(EstadoDispositivo e) {
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
                .build();
    }
}