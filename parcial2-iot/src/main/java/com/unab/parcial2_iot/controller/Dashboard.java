package com.unab.parcial2_iot.controller;

import com.unab.parcial2_iot.services.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class Dashboard {
    private final DashboardService dashboardService;

    @GetMapping({"/", "/dashboard"})
    public String showDashboard(Model model, @org.springframework.web.bind.annotation.RequestParam(name = "range", required = false) String range) {
        model.addAttribute("titulo", "Dashboard");
        model.addAttribute("nav", "dashboard");
        model.addAttribute("kpis", dashboardService.getKpis());
        model.addAttribute("telemetrySeries", dashboardService.telemetrySeries(range));
        model.addAttribute("alertsSeries", dashboardService.alertsSeries(range));
        model.addAttribute("topDevices", dashboardService.topDevices(range, 5));
        model.addAttribute("range", (range == null ? "24h" : range));
        return "Dashboard/dashboard"; // Thymeleaf abre dashboard.html
    }
}
