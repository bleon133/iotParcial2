package com.unab.parcial2_iot.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "app.data")
public class RetentionProps {
    /** Habilita eliminación por antigüedad */
    private boolean retentionEnabled = true;
    /** Días a conservar en telemetría */
    private int retentionDays = 90;

    /** Habilita generación de rollups */
    private boolean rollupsEnabled = true;
}

