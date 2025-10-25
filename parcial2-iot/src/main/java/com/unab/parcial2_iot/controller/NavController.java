package com.unab.parcial2_iot.controller;

import org.springframework.web.bind.annotation.GetMapping;

public class NavController {
    @GetMapping("/dashboard")
    public String mostrarDashboard() {
        return "Dashboard/dashboard"; // Ruta dentro de templates
    }

    @GetMapping("/plantillas")
    public String mostrarPlantillas() {
        return "plantillas/crear"; // Ruta dentro de templates
    }
}

