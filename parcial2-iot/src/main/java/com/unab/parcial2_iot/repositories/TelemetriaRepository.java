package com.unab.parcial2_iot.repositories;

import com.unab.parcial2_iot.models.Telemetria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface TelemetriaRepository extends JpaRepository<Telemetria, Long> {

    interface TelemetriaRow {
        java.util.UUID getDispositivoId();

        java.util.UUID getVariableId();

        java.time.Instant getTs();       // dejémoslo como Instant

        Double getValorNumero();

        Boolean getValorBooleano();

        String getValorTexto();

        String getValorJson();           // <-- CAMBIAR a String
    }

    @Query(value = """
                select
                  t.dispositivo_id          as dispositivoId,
                  t.variable_plantilla_id   as variableId,
                  t.ts                      as ts,
                  t.valor_numero            as valorNumero,
                  t.valor_booleano          as valorBooleano,
                  t.valor_texto             as valorTexto,
                  (t.valor_json)::text      as valorJson     -- <-- cast a texto
                from iot.telemetria t
                where t.dispositivo_id = :disp
                  and t.variable_plantilla_id = :var
                order by t.ts desc
                limit :limit
            """, nativeQuery = true)
    List<TelemetriaRow> ultimas(UUID disp, UUID var, int limit);
}