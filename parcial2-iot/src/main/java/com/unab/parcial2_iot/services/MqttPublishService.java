package com.unab.parcial2_iot.services;

import com.unab.parcial2_iot.config.MqttOutboundConfig;
import com.unab.parcial2_iot.config.MqttProps;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MqttPublishService {
    private final MqttOutboundConfig.MqttGateway gateway;
    private final MqttProps props;

    public void publish(String topic, String payload) {
        gateway.send(payload, topic, props.getQos());
    }
}

