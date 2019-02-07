package com.scottrbrtsn.websocket.chat.services.impl;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.internal.Primitives;
import com.scottrbrtsn.websocket.chat.message.OutputMessage;
import com.scottrbrtsn.websocket.chat.services.IMessageService;
import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.Queue;

@Service
public class MessageService implements IMessageService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MessageService.class);

    private Queue<Object> messages = new LinkedList<>();

    @Autowired
    SimpMessagingTemplate simpMessagingTemplate;

    public <T> void subscribeToMessage(String mqTopic, Class<T> classOfT, IMqttClient subscriber) {
        LOGGER.info("Subscribing to : {}", mqTopic);

        if (subscriber != null) {
            try {
                Gson g = new Gson();
                if (!subscriber.isConnected()) {
                    subscriber.connect();
                }

                subscriber.subscribe(mqTopic, (topic, msg) -> {
                    LOGGER.debug("PAYLOAD: {}", msg);
                    LOGGER.debug("TOPIC: {}", topic);
                    LOGGER.debug("CLASS: {}", classOfT);
                    try {
                        Object message = g.fromJson(msg.toString(), classOfT);
                        messages.add(Primitives.wrap(classOfT).cast(message));
                        sendMessageToWebSocket(msg.toString());
                    } catch (Exception e) {
                        e.printStackTrace();
                        LOGGER.error("Object didn't map as expected, trying a list");
                        try {
                            LOGGER.debug("Trying as a list");
                            JsonArray messageList = g.fromJson(msg.toString(), JsonArray.class);
                            messageList.forEach(message -> messages.add(
                                    Primitives.wrap(classOfT)
                                            .cast(g.fromJson(message.toString(), classOfT))));
                            LOGGER.debug("Exception was thrown, restarting the subscriber thread.");
                            this.subscribeToMessage(mqTopic, classOfT, subscriber);
                            sendMessageToWebSocket(msg.toString());
                        } catch (ClassCastException e2) {
                            e2.printStackTrace();
                            LOGGER.error("Object receieved in unexpected state.");
                        }
                    }
                });
            } catch (MqttException me) {
                LOGGER.error("Mqtt failed to subscribe");
            }
        }
    }

    @Override
    public Queue<Object> getMessages() {
        return this.messages;
    }

    @Override
    public void sendMessage(String json, IMqttClient client, String topic) {
        byte[] payload = json.getBytes();
        MqttMessage msg = new MqttMessage(payload);
        msg.setQos(0);
        msg.setRetained(true);
        try {
            client.publish(topic, msg);
        } catch (MqttException me) {
            LOGGER.error("Couldn't publish to the client.");
            LOGGER.error(me.getMessage());
        }
    }

    private void sendMessageToWebSocket(String message) {
        final String time = new SimpleDateFormat("HH:mm").format(new Date());
        simpMessagingTemplate.convertAndSend("/topic/messages", new OutputMessage("sentFromServer", message, time));
    }
}