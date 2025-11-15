package com.unab.parcial2_iot.config;

import lombok.RequiredArgsConstructor;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.integration.annotation.IntegrationComponentScan;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.mqtt.core.DefaultMqttPahoClientFactory;
import org.springframework.integration.mqtt.core.MqttPahoClientFactory;
import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter;
import org.springframework.integration.mqtt.support.DefaultPahoMessageConverter;

import java.util.UUID;

@Configuration
@EnableIntegration
@IntegrationComponentScan
@EnableConfigurationProperties(MqttProps.class)
@RequiredArgsConstructor
public class MqttConfig {

    private final MqttProps props;

    @Bean
    public MqttPahoClientFactory mqttClientFactory() {
        var factory = new DefaultMqttPahoClientFactory();
        var options = new MqttConnectOptions();
        options.setServerURIs(new String[]{props.getUrl()});
        if (props.getUsername() != null && !props.getUsername().isBlank()) {
            options.setUserName(props.getUsername());
        }
        if (props.getPassword() != null) {
            options.setPassword(props.getPassword().toCharArray());
        }
        options.setAutomaticReconnect(true);
        options.setCleanSession(true);
        factory.setConnectionOptions(options);
        return factory;
    }

    @Bean
    public IntegrationFlow mqttInboundFlow(MqttPahoClientFactory cf, TelemetriaMqttHandler handler) {
        String clientId = "api-inbound-" + UUID.randomUUID();
        var topics = props.getInboundTopics().toArray(String[]::new);

        var adapter = new MqttPahoMessageDrivenChannelAdapter(clientId, cf, topics);
        var converter = new DefaultPahoMessageConverter(props.getQos(), false); // payload como String
        adapter.setConverter(converter);
        adapter.setQos(props.getQos());
        adapter.setAutoStartup(true);

        // Usar IntegrationFlow.from en lugar de IntegrationFlows.from
        return IntegrationFlow.from(adapter)
                .handle(handler)
                .get();
    }

    @Bean
    public IntegrationFlow mqttAckInboundFlow(MqttPahoClientFactory cf, com.unab.parcial2_iot.config.handlers.ComandoMqttAckHandler handler) {
        String clientId = "api-ack-" + UUID.randomUUID();
        String[] topics;
        if (props.getAckTopics() != null && !props.getAckTopics().isEmpty()) {
            topics = props.getAckTopics().toArray(String[]::new);
        } else {
            topics = new String[]{"devices/+/commands/ack"};
        }

        var adapter = new MqttPahoMessageDrivenChannelAdapter(clientId, cf, topics);
        var converter = new DefaultPahoMessageConverter(props.getQos(), false);
        adapter.setConverter(converter);
        adapter.setQos(props.getQos());
        adapter.setAutoStartup(true);

        return IntegrationFlow.from(adapter)
                .handle(handler)
                .get();
    }
}
