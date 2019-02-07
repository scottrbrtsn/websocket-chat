package com.scottrbrtsn.websocket.chat.services;

import org.eclipse.paho.client.mqttv3.IMqttClient;

import java.util.Queue;

public interface IMessageService {
    <T> void subscribeToMessage(String mqTopic, Class<T> classOfT, IMqttClient subscriber);

    void sendMessage(String json, IMqttClient client, String topic);

    Queue<Object> getMessages();

}
