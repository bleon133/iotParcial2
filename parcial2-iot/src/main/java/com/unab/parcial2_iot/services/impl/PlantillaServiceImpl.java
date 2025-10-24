package com.unab.parcial2_iot.services.impl;

import com.unab.parcial2_iot.models.Plantilla;
import com.unab.parcial2_iot.repositories.PlantillaRepository;
import com.unab.parcial2_iot.services.PlantillaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PlantillaServiceImpl implements PlantillaService {

    private final PlantillaRepository repo;

    @Override
    public List<Plantilla> listar() {
        return repo.findAll();
    }

    @Override
    @Transactional
    public Plantilla crear(Plantilla nueva) {
        if (nueva == null) throw new IllegalArgumentException("Plantilla requerida");
        if (nueva.getNombre() == null || nueva.getNombre().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre es obligatorio");
        }
        final String nombre = nueva.getNombre().trim();
        if (repo.existsByNombreIgnoreCase(nombre)) {
            throw new IllegalStateException("Ya existe una plantilla con ese nombre");
        }
        nueva.setNombre(nombre);

        // Asegurar creadoEn si la entidad no lo trae
        if (nueva.getCreadoEn() == null) {
            nueva.setCreadoEn(OffsetDateTime.now());
        }

        return repo.save(nueva);
    }

    @Override
    public Optional<Plantilla> buscarPorId(UUID id) {
        return repo.findById(id);
    }

    @Override
    @Transactional
    public Plantilla actualizar(UUID id, Plantilla cambios) {
        Plantilla actual = repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Plantilla no encontrada"));

        if (cambios.getNombre() != null) {
            String nuevoNombre = cambios.getNombre().trim();
            if (nuevoNombre.isEmpty()) {
                throw new IllegalArgumentException("El nombre es obligatorio");
            }
            if (!nuevoNombre.equalsIgnoreCase(actual.getNombre())
                    && repo.existsByNombreIgnoreCase(nuevoNombre)) {
                throw new IllegalStateException("Ya existe una plantilla con ese nombre");
            }
            actual.setNombre(nuevoNombre);
        }
        if (cambios.getDescripcion() != null) {
            actual.setDescripcion(cambios.getDescripcion());
        }
        if (cambios.getProtocoloPredeterminado() != null) {
            actual.setProtocoloPredeterminado(cambios.getProtocoloPredeterminado());
        }
        return actual; // dirty checking
    }

    @Override
    @Transactional
    public void eliminar(UUID id) {
        repo.deleteById(id);
    }
}