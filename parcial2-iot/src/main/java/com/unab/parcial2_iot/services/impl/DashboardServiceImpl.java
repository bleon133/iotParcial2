package com.unab.parcial2_iot.services.impl;

import com.unab.parcial2_iot.dto.ChartSeries;
import com.unab.parcial2_iot.dto.KpisOut;
import com.unab.parcial2_iot.repositories.AlertaRepository;
import com.unab.parcial2_iot.repositories.DispositivoRepository;
import com.unab.parcial2_iot.repositories.TelemetriaRepository;
import com.unab.parcial2_iot.services.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final DispositivoRepository dispositivoRepo;
    private final TelemetriaRepository teleRepo;
    private final AlertaRepository alertaRepo;

    @Override
    public KpisOut getKpis() {
        long total = dispositivoRepo.count();
        OffsetDateTime now = OffsetDateTime.now();
        long online = teleRepo.countDevicesOnlineSince(now.minusMinutes(5));
        long alertas24h = alertaRepo.countByTsAfter(now.minusHours(24));
        OffsetDateTime inicioDia = LocalDate.now().atStartOfDay().atOffset(now.getOffset());
        long muestrasHoy = teleRepo.countTelemetrySince(inicioDia);
        return new KpisOut(total, online, alertas24h, muestrasHoy);
    }

    @Override
    public ChartSeries telemetrySeries(String range) {
        ZoneId zone = ZoneId.systemDefault();
        String r = (range == null ? "24h" : range.toLowerCase());
        if (!r.equals("6h") && !r.equals("24h") && !r.equals("7d")) r = "24h";

        OffsetDateTime now = OffsetDateTime.now(zone);
        if (r.equals("7d")) {
            OffsetDateTime since = now.minusDays(6).withHour(0).withMinute(0).withSecond(0).withNano(0);
            var rows = teleRepo.countByDaySince(since);
            var map = new java.util.HashMap<OffsetDateTime, Long>();
            for (var rr : rows) {
                var odt = OffsetDateTime.ofInstant(rr.getBucket(), zone).withHour(0).withMinute(0).withSecond(0).withNano(0);
                map.put(odt, rr.getCnt());
            }
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MM-dd");
            var labels = new java.util.ArrayList<String>();
            var data = new java.util.ArrayList<Long>();
            OffsetDateTime cursor = since;
            for (int i = 0; i <= 6; i++) {
                labels.add(cursor.format(fmt));
                data.add(map.getOrDefault(cursor, 0L));
                cursor = cursor.plusDays(1);
            }
            return ChartSeries.builder().labels(labels).data(data).build();
        } else {
            int hours = r.equals("6h") ? 6 : 24;
            OffsetDateTime since = now.minusHours(hours);
            var rows = teleRepo.countByHourSince(since);
            var map = new java.util.HashMap<OffsetDateTime, Long>();
            for (var rr : rows) {
                var odt = OffsetDateTime.ofInstant(rr.getBucket(), zone).withMinute(0).withSecond(0).withNano(0);
                map.put(odt, rr.getCnt());
            }
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MM-dd HH:mm");
            var labels = new java.util.ArrayList<String>();
            var data = new java.util.ArrayList<Long>();
            OffsetDateTime cursor = since.withMinute(0).withSecond(0).withNano(0);
            for (int i = 0; i <= hours; i++) {
                labels.add(cursor.format(fmt));
                data.add(map.getOrDefault(cursor, 0L));
                cursor = cursor.plusHours(1);
            }
            return ChartSeries.builder().labels(labels).data(data).build();
        }
    }

    @Override
    public ChartSeries alertsSeries(String range) {
        ZoneId zone = ZoneId.systemDefault();
        String r = (range == null ? "24h" : range.toLowerCase());
        if (!r.equals("6h") && !r.equals("24h") && !r.equals("7d")) r = "24h";
        OffsetDateTime now = OffsetDateTime.now(zone);
        if (r.equals("7d")) {
            OffsetDateTime since = now.minusDays(6).withHour(0).withMinute(0).withSecond(0).withNano(0);
            var rows = alertaRepo.countByDaySince(since);
            var map = new java.util.HashMap<OffsetDateTime, Long>();
            for (var rr : rows) {
                var odt = OffsetDateTime.ofInstant(rr.getBucket(), zone).withHour(0).withMinute(0).withSecond(0).withNano(0);
                map.put(odt, rr.getCnt());
            }
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MM-dd");
            var labels = new java.util.ArrayList<String>();
            var data = new java.util.ArrayList<Long>();
            OffsetDateTime cursor = since;
            for (int i = 0; i <= 6; i++) {
                labels.add(cursor.format(fmt));
                data.add(map.getOrDefault(cursor, 0L));
                cursor = cursor.plusDays(1);
            }
            return ChartSeries.builder().labels(labels).data(data).build();
        } else {
            int hours = r.equals("6h") ? 6 : 24;
            OffsetDateTime since = now.minusHours(hours);
            var rows = alertaRepo.countByHourSince(since);
            var map = new java.util.HashMap<OffsetDateTime, Long>();
            for (var rr : rows) {
                var odt = OffsetDateTime.ofInstant(rr.getBucket(), zone).withMinute(0).withSecond(0).withNano(0);
                map.put(odt, rr.getCnt());
            }
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MM-dd HH:mm");
            var labels = new java.util.ArrayList<String>();
            var data = new java.util.ArrayList<Long>();
            OffsetDateTime cursor = since.withMinute(0).withSecond(0).withNano(0);
            for (int i = 0; i <= hours; i++) {
                labels.add(cursor.format(fmt));
                data.add(map.getOrDefault(cursor, 0L));
                cursor = cursor.plusHours(1);
            }
            return ChartSeries.builder().labels(labels).data(data).build();
        }
    }

    @Override
    public ChartSeries topDevices(String range, int limit) {
        String r = (range == null ? "24h" : range.toLowerCase());
        if (!r.equals("6h") && !r.equals("24h") && !r.equals("7d")) r = "24h";
        OffsetDateTime since = switch (r) {
            case "6h" -> OffsetDateTime.now().minusHours(6);
            case "7d" -> OffsetDateTime.now().minusDays(7);
            default -> OffsetDateTime.now().minusHours(24);
        };
        var rows = teleRepo.topDevicesSince(since, Math.max(1, Math.min(10, limit)));
        return ChartSeries.builder()
                .labels(rows.stream().map(TelemetriaRepository.DeviceCountRow::getDispositivoNombre).collect(Collectors.toList()))
                .data(rows.stream().map(TelemetriaRepository.DeviceCountRow::getCnt).collect(Collectors.toList()))
                .build();
    }
}
