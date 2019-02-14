package com.scottrbrtsn.websocket.chat.config;

import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.UUID;

@Configuration
public class MqttConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(MqttConfig.class);

    private static final String subAddr = "tcp://localhost:61617";
    private static final String pubAddr = "tcp://localhost:61627";

    @Bean
    public IMqttClient subscriber(){
        return newClient(subAddr);
    }
    @Bean
    public IMqttClient publisher(){
        return newClient(pubAddr);
    }

    private IMqttClient newClient(String serverURI){
        String publisherId = UUID.randomUUID().toString();
        IMqttClient client = null;
        try {
            client = new MqttClient(serverURI, publisherId);
            MqttConnectOptions options = new MqttConnectOptions();
            options.setAutomaticReconnect(true);
            options.setCleanSession(true);
            options.setConnectionTimeout(10);
            client.connect();
        }catch (MqttException e){
            LOGGER.error("Error connecting the MqttClient");
        }
        return client;
    }

}
