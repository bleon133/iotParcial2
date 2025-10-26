package com.unab.parcial2_iot.services.impl;

import com.unab.parcial2_iot.models.Dispositivo;
import com.unab.parcial2_iot.models.Plantilla;
import com.unab.parcial2_iot.models.Protocolo;
import com.unab.parcial2_iot.repositories.DispositivoRepository;
import com.unab.parcial2_iot.services.DispositivoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DispositivoServiceImpl implements DispositivoService {

    private final DispositivoRepository repo;

    @Override
    public List<Dispositivo> listar() {
        return repo.findAllByOrderByCreadoEnDesc();
    }

    @Override
    @Transactional
    public Dispositivo crear(Dispositivo nuevo) {
        if (nuevo == null) throw new IllegalArgumentException("Dispositivo requerido");
        if (nuevo.getPlantilla() == null || nuevo.getPlantilla().getId() == null) {
            throw new IllegalArgumentException("La plantilla es obligatoria");
        }
        if (isBlank(nuevo.getIdExterno())) throw new IllegalArgumentException("id_externo es obligatorio");
        if (isBlank(nuevo.getNombre())) throw new IllegalArgumentException("nombre es obligatorio");
        if (nuevo.getTipo() == null) throw new IllegalArgumentException("tipo es obligatorio");

        final String idExt = nuevo.getIdExterno().trim();
        if (repo.existsByIdExternoIgnoreCase(idExt)) {
            throw new IllegalStateException("Ya existe un dispositivo con id_externo=" + idExt);
        }
        nuevo.setIdExterno(idExt);

        // Rellenos/por defecto
        if (isBlank(nuevo.getEstado())) {
            nuevo.setEstado("habilitado");
        }

        // Si no se envía protocolo, usar el predeterminado de la plantilla (si viene cargada) o mqtt
        Plantilla p = nuevo.getPlantilla();
        if (nuevo.getProtocolo() == null) {
            Protocolo proto = (p != null && p.getProtocoloPredeterminado() != null)
                    ? p.getProtocoloPredeterminado()
                    : Protocolo.mqtt;
            nuevo.setProtocolo(proto);
        }

        // Validar lat/lon si llegan
        if (nuevo.getLatitud() != null && (nuevo.getLatitud() < -90 || nuevo.getLatitud() > 90)) {
            throw new IllegalArgumentException("Latitud fuera de rango [-90,90]");
        }
        if (nuevo.getLongitud() != null && (nuevo.getLongitud() < -180 || nuevo.getLongitud() > 180)) {
            throw new IllegalArgumentException("Longitud fuera de rango [-180,180]");
        }

        if (nuevo.getCreadoEn() == null) {
            nuevo.setCreadoEn(OffsetDateTime.now());
        }

        return repo.save(nuevo);
    }

    @Override
    @Transactional
    public void eliminar(UUID id) {
        repo.deleteById(id);
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}