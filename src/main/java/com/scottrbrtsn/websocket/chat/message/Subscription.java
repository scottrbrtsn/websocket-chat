package com.scottrbrtsn.websocket.chat.message;

import com.coxautodev.graphql.tools.GraphQLSubscriptionResolver;
import com.scottrbrtsn.websocket.chat.services.impl.MessageService;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
class Subscription implements GraphQLSubscriptionResolver {

    @Autowired
    private MessageService messageService;

    Publisher<OutputMessage> subMessages(List<String> topics) {
        return messageService.getPublisher(topics);
    }

}