package com.scottrbrtsn.websocket.chat.services.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.internal.Primitives;
import com.scottrbrtsn.websocket.chat.message.OutputMessage;
import com.scottrbrtsn.websocket.chat.services.IMessageService;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.observables.ConnectableObservable;
import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
public class MessageService implements IMessageService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MessageService.class);

    private Queue<Object> messages = new LinkedList<>();

    @Autowired
    IMqttClient subscriber;

    @Autowired
    SimpMessagingTemplate simpMessagingTemplate;

    private final Flowable<OutputMessage> publisher;
    private List<OutputMessage> outputMessages = new ArrayList<>();


    /**
     * from the constructor to getPublisher()
     * This is a way to broadcast using GraphQL via WS directly
     * i.e. not using stomp.
     */
    public MessageService() {
        Observable<OutputMessage> outputMessageObservable = Observable.create(emitter -> {

            ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);
            executorService.scheduleAtFixedRate(newMessage(emitter), 0, 2, TimeUnit.SECONDS);

        });

        ConnectableObservable<OutputMessage> connectableObservable = outputMessageObservable.share().publish();
        connectableObservable.connect();

        publisher = connectableObservable.toFlowable(BackpressureStrategy.BUFFER);

        this.subscribeToMessage("myActiveMQTopic", Object.class, subscriber);
    }

    private synchronized Runnable newMessage(ObservableEmitter<OutputMessage> emitter) {
        return () -> {
            if (outputMessages != null) {
                emitMessages(emitter, outputMessages);
            }
        };
    }

    private void emitMessages(ObservableEmitter<OutputMessage> emitter, List<OutputMessage> messageUpdates) {
        for(OutputMessage message : messageUpdates) {
            try {
                emitter.onNext(message);
                outputMessages = new ArrayList<>();
            } catch (RuntimeException e) {
                LOGGER.error("Cannot send Message to Websocket", e);
            }
        }
    }

    public Flowable<OutputMessage> getPublisher(List<String> topics) {
        if (topics != null) {
            return publisher.filter(message -> topics.contains(message.getFrom()));
        }
        return publisher;
    }

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
                        T castObject = Primitives.wrap(classOfT).cast(message);
                        messages.add(castObject);
                        sendMessageToStompWebSocket(castObject);
                        addMessageToLists(castObject, mqTopic);//sends to be broadcast to GraphQL subscribers
                    } catch (Exception e) {
                        LOGGER.error("Object didn't map as expected, trying a list", e);
                        try {
                            LOGGER.debug("Trying as a list");
                            JsonArray messageList = g.fromJson(msg.toString(), JsonArray.class);
                            messageList.forEach(message -> {
                                T castObject = Primitives.wrap(classOfT)
                                        .cast(g.fromJson(message.toString(), classOfT));
                                messages.add(castObject);
                                sendMessageToStompWebSocket(castObject);
                                addMessageToLists(castObject, mqTopic);//sends to be broadcast to GraphQL subscribers
                            });
                            LOGGER.debug("Exception was thrown, restarting the subscriber thread.");
                            this.subscribeToMessage(mqTopic, classOfT, subscriber);
                        } catch (ClassCastException e2) {
                            LOGGER.error("Object receieved in unexpected state.", e2);
                        }
                    }
                });
            } catch (MqttException me) {
                LOGGER.error("Mqtt failed to subscribe");
            }
        }
    }

    @SuppressWarnings("unchecked")
    private <T> void addMessageToLists(T castObject, String mqTopic){
        ObjectMapper oMapper = new ObjectMapper();
        try {
            Map<String, Object> castMap = oMapper.convertValue(castObject, Map.class);
            final String time = new SimpleDateFormat("HH:mm").format(new Date());
            OutputMessage outputMessage = new OutputMessage(castMap, mqTopic, time);
            outputMessages.add(outputMessage);
            messages.add(castObject);//cache
        } catch (Exception e) {
            LOGGER.error(e.getMessage());

        }

        //sendMessage(castObject.toString(), pub, mqTopic);//this is to send it from the interface to Apollo
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

    @SuppressWarnings("unchecked")
    private void sendMessageToStompWebSocket(Object message) {
        ObjectMapper oMapper = new ObjectMapper();
        try {
            Map<String, Object> castMap = oMapper.convertValue(message, Map.class);
            final String time = new SimpleDateFormat("HH:mm").format(new Date());
            simpMessagingTemplate.convertAndSend("/topic/messages", new OutputMessage(castMap, "sentFromServer", time));
        }catch (Exception e ){
            LOGGER.error(e.getMessage());
        }
    }
}