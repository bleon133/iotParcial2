package com.unab.parcial2_iot.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.MessagingGateway;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.mqtt.core.MqttPahoClientFactory;
import org.springframework.integration.mqtt.outbound.MqttPahoMessageHandler;
import org.springframework.integration.mqtt.support.MqttHeaders;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;

import java.util.UUID;

@Configuration
@RequiredArgsConstructor
public class MqttOutboundConfig {

    private final MqttProps props;

    @Bean
    public MessageChannel mqttOutboundChannel() { return new DirectChannel(); }

    @Bean
    public MessageHandler mqttOutboundHandler(MqttPahoClientFactory cf) {
        String clientId = "api-outbound-" + UUID.randomUUID();
        var handler = new MqttPahoMessageHandler(clientId, cf);
        handler.setAsync(true);
        handler.setDefaultQos(props.getQos());
        return handler;
    }

    @Bean
    public IntegrationFlow mqttOutboundFlow(MessageHandler mqttOutboundHandler) {
        return f -> f.channel(mqttOutboundChannel()).handle(mqttOutboundHandler);
    }

    @MessagingGateway(defaultRequestChannel = "mqttOutboundChannel")
    public interface MqttGateway {
        void send(String payload,
                  @org.springframework.messaging.handler.annotation.Header(MqttHeaders.TOPIC) String topic,
                  @org.springframework.messaging.handler.annotation.Header(MqttHeaders.QOS) int qos);
    }
}

