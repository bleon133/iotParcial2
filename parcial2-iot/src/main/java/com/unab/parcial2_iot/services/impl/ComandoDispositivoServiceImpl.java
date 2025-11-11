package com.unab.parcial2_iot.services.impl;

import com.unab.parcial2_iot.models.ComandoDispositivo;
import com.unab.parcial2_iot.models.Dispositivo;
import com.unab.parcial2_iot.models.EstadoComando;
import com.unab.parcial2_iot.repositories.ComandoDispositivoRepository;
import com.unab.parcial2_iot.repositories.DispositivoRepository;
import com.unab.parcial2_iot.services.ComandoDispositivoService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class ComandoDispositivoServiceImpl implements ComandoDispositivoService {

    private final ComandoDispositivoRepository comandoRepo;
    private final DispositivoRepository dispositivoRepo;

    @Override
    public ComandoDispositivo crear(UUID dispositivoId,
                                    String comando,
                                    String datos,
                                    String solicitadoPor) {

        var disp = dispositivoRepo.findById(dispositivoId)
                .orElseThrow(() -> new EntityNotFoundException("Dispositivo no encontrado"));

        String datosJson = (datos == null || datos.isBlank()) ? null : datos;

        ComandoDispositivo cmd = ComandoDispositivo.builder()
                .dispositivo(disp)
                .comando(comando)
                .datos(datosJson)                    // debe ser JSON válido si no es null
                .estado(EstadoComando.ENVIADO)       // ahora sí mapea al enum PG
                .solicitadoPor(solicitadoPor)
                .solicitadoEn(OffsetDateTime.now())
                .build();

        return comandoRepo.save(cmd);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ComandoDispositivo> historialPorDispositivo(UUID dispositivoId) {
        if (dispositivoId != null) {
            return comandoRepo.findByDispositivoIdOrderBySolicitadoEnDesc(dispositivoId);
        }
        return comandoRepo.findAllByOrderBySolicitadoEnDesc();
    }

    @Override
    public ComandoDispositivo cambiarEstado(UUID comandoId,
                                            EstadoComando nuevoEstado,
                                            String mensajeError) {

        ComandoDispositivo cmd = comandoRepo.findById(comandoId)
                .orElseThrow(() -> new EntityNotFoundException("Comando no encontrado"));

        cmd.setEstado(nuevoEstado);

        if (nuevoEstado == EstadoComando.ACK
                || nuevoEstado == EstadoComando.ERROR
                || nuevoEstado == EstadoComando.TIEMPO_AGOTADO
                || nuevoEstado == EstadoComando.CANCELADO) {
            cmd.setConfirmadoEn(OffsetDateTime.now());
        }

        if (mensajeError != null && !mensajeError.isBlank()) {
            cmd.setMensajeError(mensajeError);
        }

        return comandoRepo.save(cmd);
    }
}
