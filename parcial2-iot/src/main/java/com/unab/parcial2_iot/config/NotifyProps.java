package com.unab.parcial2_iot.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "app.notify")
public class NotifyProps {
    private boolean enabled = false;
    private String webhookUrl;
}

