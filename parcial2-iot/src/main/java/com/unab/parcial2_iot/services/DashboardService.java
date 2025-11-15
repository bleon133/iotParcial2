package com.unab.parcial2_iot.services;

import com.unab.parcial2_iot.dto.ChartSeries;
import com.unab.parcial2_iot.dto.KpisOut;

public interface DashboardService {
    KpisOut getKpis();

    ChartSeries telemetrySeries(String range); // 6h | 24h | 7d

    ChartSeries alertsSeries(String range);

    ChartSeries topDevices(String range, int limit);
}
