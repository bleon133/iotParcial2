package com.unab.parcial2_iot.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class Dashboard {
    @GetMapping("/dashboard")
    public String showDashboard(Model model) {
        return "Dashboard/dashboard"; // Thymeleaf abre dashboard.html
    }
}
