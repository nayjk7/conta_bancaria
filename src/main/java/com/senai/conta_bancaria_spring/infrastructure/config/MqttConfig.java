package com.senai.conta_bancaria_spring.infrastructure.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.senai.conta_bancaria_spring.application.DTO.ValidacaoAuthDTO;
import com.senai.conta_bancaria_spring.application.service.IoTService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.core.MessageProducer;
import org.springframework.integration.mqtt.core.DefaultMqttPahoClientFactory;
import org.springframework.integration.mqtt.core.MqttPahoClientFactory;
import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter;
import org.springframework.integration.mqtt.outbound.MqttPahoMessageHandler;
import org.springframework.integration.mqtt.support.DefaultPahoMessageConverter;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;

@Configuration
public class MqttConfig {
    @Value("${mqtt.broker.url}")
    private String brokerUrl;

    @Value("${mqtt.topic.validacao}")
    private String topicValidacao;

    @Bean
    public MqttPahoClientFactory mqttClientFactory() {
        DefaultMqttPahoClientFactory factory = new DefaultMqttPahoClientFactory();

        // Configuração das opções de conexão
        MqttConnectOptions options = new MqttConnectOptions();
        options.setServerURIs(new String[]{brokerUrl});
        options.setConnectionTimeout(10); // Timeout em segundos
        options.setAutomaticReconnect(true); // Recomendado: reconectar automaticamente
        options.setCleanSession(true);

        factory.setConnectionOptions(options);
        return factory;
    }
    @Bean
    public MessageChannel mqttOutboundChannel() {
        return new DirectChannel();
    }

    @Bean
    @ServiceActivator(inputChannel = "mqttOutboundChannel")
    public MessageHandler mqttOutbound() {
        MqttPahoMessageHandler messageHandler =
                new MqttPahoMessageHandler("producerClient", mqttClientFactory());
        messageHandler.setAsync(true);
        messageHandler.setDefaultTopic("banco/autenticacao");
        return messageHandler;
    }

    @Bean
    public MessageChannel mqttInputChannel() {
        return new DirectChannel();
    }

    @Bean
    public MessageProducer inbound() {
        MqttPahoMessageDrivenChannelAdapter adapter =
                new MqttPahoMessageDrivenChannelAdapter("consumerClient", mqttClientFactory(), topicValidacao);
        adapter.setCompletionTimeout(5000);
        adapter.setConverter(new DefaultPahoMessageConverter());
        adapter.setQos(1);
        adapter.setOutputChannel(mqttInputChannel());
        return adapter;
    }

    @Bean
    @ServiceActivator(inputChannel = "mqttInputChannel")
    public MessageHandler handler(IoTService ioTService, ObjectMapper objectMapper) {
        return message -> {
            try {
                String payload = (String) message.getPayload();
                System.out.println("Payload recebido via MQTT: " + payload);

                // Converte o JSON recebido para o DTO
                ValidacaoAuthDTO dto = objectMapper.readValue(payload, ValidacaoAuthDTO.class);

                // Chama o service
                ioTService.processarCodigoRecebido(dto);
            } catch (Exception e) {
                e.printStackTrace();
            }
        };
    }
}