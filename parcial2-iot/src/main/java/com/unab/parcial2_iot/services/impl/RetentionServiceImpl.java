package com.unab.parcial2_iot.services.impl;

import com.unab.parcial2_iot.config.RetentionProps;
import com.unab.parcial2_iot.services.RetentionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Service
@RequiredArgsConstructor
@Slf4j
public class RetentionServiceImpl implements RetentionService {

    private final JdbcTemplate jdbc;
    private final RetentionProps props;

    @Override
    @Scheduled(cron = "0 3 * * * *") // cada hora al mm=03 para evitar solapar con on-the-hour
    @Transactional
    public void rollupHourly() {
        if (!props.isRollupsEnabled()) return;
        // Hora completa anterior
        OffsetDateTime end = OffsetDateTime.now(ZoneOffset.UTC).withMinute(0).withSecond(0).withNano(0);
        OffsetDateTime start = end.minusHours(1);
        String sql = "INSERT INTO iot.telemetria_rollup_hourly (dispositivo_id, variable_plantilla_id, bucket_ts, cnt, avg, min, max)\n" +
                "SELECT t.dispositivo_id, t.variable_plantilla_id, date_trunc('hour', t.ts) AS bucket_ts,\n" +
                "       count(*)::bigint, avg(t.valor_numero), min(t.valor_numero), max(t.valor_numero)\n" +
                "FROM iot.telemetria t\n" +
                "WHERE t.ts >= ? AND t.ts < ? AND t.valor_numero IS NOT NULL\n" +
                "GROUP BY t.dispositivo_id, t.variable_plantilla_id, date_trunc('hour', t.ts)\n" +
                "ON CONFLICT (dispositivo_id, variable_plantilla_id, bucket_ts) DO UPDATE\n" +
                "SET cnt = EXCLUDED.cnt, avg = EXCLUDED.avg, min = EXCLUDED.min, max = EXCLUDED.max";
        int n = jdbc.update(sql, ps -> {
            ps.setObject(1, start);
            ps.setObject(2, end);
        });
        log.debug("rollupHourly upserts={} bucket={}..{}", n, start, end);
    }

    @Override
    @Scheduled(cron = "0 7 0 * * *") // diario, poco después de medianoche UTC
    @Transactional
    public void rollupDaily() {
        if (!props.isRollupsEnabled()) return;
        OffsetDateTime end = OffsetDateTime.now(ZoneOffset.UTC).withHour(0).withMinute(0).withSecond(0).withNano(0);
        OffsetDateTime start = end.minusDays(1);
        String sql = "INSERT INTO iot.telemetria_rollup_daily (dispositivo_id, variable_plantilla_id, bucket_ts, cnt, avg, min, max)\n" +
                "SELECT t.dispositivo_id, t.variable_plantilla_id, date_trunc('day', t.ts) AS bucket_ts,\n" +
                "       count(*)::bigint, avg(t.valor_numero), min(t.valor_numero), max(t.valor_numero)\n" +
                "FROM iot.telemetria t\n" +
                "WHERE t.ts >= ? AND t.ts < ? AND t.valor_numero IS NOT NULL\n" +
                "GROUP BY t.dispositivo_id, t.variable_plantilla_id, date_trunc('day', t.ts)\n" +
                "ON CONFLICT (dispositivo_id, variable_plantilla_id, bucket_ts) DO UPDATE\n" +
                "SET cnt = EXCLUDED.cnt, avg = EXCLUDED.avg, min = EXCLUDED.min, max = EXCLUDED.max";
        int n = jdbc.update(sql, ps -> {
            ps.setObject(1, start);
            ps.setObject(2, end);
        });
        log.debug("rollupDaily upserts={} bucket={}..{}", n, start, end);
    }

    @Override
    @Scheduled(cron = "0 30 2 * * *") // purga diaria a las 02:30 UTC
    @Transactional
    public void purgeOld() {
        if (!props.isRetentionEnabled()) return;
        int days = Math.max(1, props.getRetentionDays());
        String sql = "DELETE FROM iot.telemetria WHERE ts < (now() AT TIME ZONE 'UTC') - INTERVAL '" + days + " days'";
        int n = jdbc.update(sql);
        log.info("purgeOld deleted={} rows (>{} days)", n, days);
    }
}

