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

    interface AggRow {
        Double getAvg();
        Double getMin();
        Double getMax();
        Long getCnt();
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

    @org.springframework.data.jpa.repository.Query(value = """
            select
              t.dispositivo_id          as dispositivoId,
              t.variable_plantilla_id   as variableId,
              t.ts                      as ts,
              t.valor_numero            as valorNumero,
              t.valor_booleano          as valorBooleano,
              t.valor_texto             as valorTexto,
              (t.valor_json)::text      as valorJson
            from iot.telemetria t
            where t.dispositivo_id = :disp
              and t.variable_plantilla_id = :var
              and t.ts >= :since and t.ts <= :until
            order by t.ts asc
            """, nativeQuery = true)
    List<TelemetriaRow> enRango(UUID disp, UUID var, java.time.OffsetDateTime since, java.time.OffsetDateTime until);

    interface TimeValueRow { java.time.Instant getTs(); Double getValorNumero(); }

    @org.springframework.data.jpa.repository.Query(value = """
            select t.ts as ts, t.valor_numero as valorNumero
            from iot.telemetria t
            where t.dispositivo_id = :disp
              and t.variable_plantilla_id = :var
              and t.valor_numero is not null
              and t.ts >= :since
            order by t.ts asc
            limit :limit
            """, nativeQuery = true)
    List<TimeValueRow> seriesNumero(UUID disp, UUID var, java.time.OffsetDateTime since, int limit);

    interface TimeBoolRow { java.time.Instant getTs(); Boolean getValorBooleano(); }

    @org.springframework.data.jpa.repository.Query(value = """
            select t.ts as ts, t.valor_booleano as valorBooleano
            from iot.telemetria t
            where t.dispositivo_id = :disp
              and t.variable_plantilla_id = :var
              and t.valor_booleano is not null
              and t.ts >= :since
            order by t.ts asc
            limit :limit
            """, nativeQuery = true)
    List<TimeBoolRow> seriesBoolean(UUID disp, UUID var, java.time.OffsetDateTime since, int limit);
    @org.springframework.data.jpa.repository.Query(value =
            "SELECT avg(valor_numero) AS avg, " +
                    "       min(valor_numero) AS min, " +
                    "       max(valor_numero) AS max, " +
                    "       count(*)          AS cnt " +
                    "FROM iot.telemetria " +
                    "WHERE dispositivo_id = :dispId " +
                    "  AND variable_plantilla_id = :varId " +
                    "  AND ts >= :since " +
                    "  AND valor_numero IS NOT NULL",
            nativeQuery = true)
    AggRow agg(java.util.UUID dispId,
               java.util.UUID varId,
               java.time.OffsetDateTime since);

    @org.springframework.data.jpa.repository.Query(value =
            "select count(distinct dispositivo_id) from iot.telemetria where ts >= :since",
            nativeQuery = true)
    long countDevicesOnlineSince(java.time.OffsetDateTime since);

    @org.springframework.data.jpa.repository.Query(value =
            "select count(*) from iot.telemetria where ts >= :since",
            nativeQuery = true)
    long countTelemetrySince(java.time.OffsetDateTime since);

    interface HourCountRow { java.time.Instant getBucket(); Long getCnt(); }

    @org.springframework.data.jpa.repository.Query(value = """
            select date_trunc('hour', ts) as bucket, count(*) as cnt
            from iot.telemetria
            where ts >= :since
            group by 1
            order by 1
            """, nativeQuery = true)
    java.util.List<HourCountRow> countByHourSince(java.time.OffsetDateTime since);

    interface DeviceCountRow {
        java.util.UUID getDispositivoId();
        String getDispositivoNombre();
        Long getCnt();
    }

    @org.springframework.data.jpa.repository.Query(value = """
            select d.id as dispositivoId, d.nombre as dispositivoNombre, count(*) as cnt
            from iot.telemetria t
            join iot.dispositivo d on d.id = t.dispositivo_id
            where t.ts >= :since
            group by d.id, d.nombre
            order by cnt desc
            limit :limit
            """, nativeQuery = true)
    java.util.List<DeviceCountRow> topDevicesSince(java.time.OffsetDateTime since, int limit);

    interface DayCountRow { java.time.Instant getBucket(); Long getCnt(); }

    @org.springframework.data.jpa.repository.Query(value = """
            select date_trunc('day', ts) as bucket, count(*) as cnt
            from iot.telemetria
            where ts >= :since
            group by 1
            order by 1
            """, nativeQuery = true)
    java.util.List<DayCountRow> countByDaySince(java.time.OffsetDateTime since);
}
