package com.unab.parcial2_iot.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@Data
@ConfigurationProperties(prefix = "app.mqtt")
public class MqttProps {
    private String url = "tcp://localhost:1883";
    private String username;
    private String password;
    private List<String> inboundTopics;
    private int qos = 1;
}