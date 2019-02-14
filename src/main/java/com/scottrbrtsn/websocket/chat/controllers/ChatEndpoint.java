package com.scottrbrtsn.websocket.chat.controllers;


import com.scottrbrtsn.websocket.chat.message.Message;
import com.scottrbrtsn.websocket.chat.message.OutputMessage;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import java.text.SimpleDateFormat;
import java.util.Date;

@Controller
public class ChatEndpoint {

    @MessageMapping("/chat")
    @SendTo("/topic/messages")
    public OutputMessage send(final Message message) {

        final String time = new SimpleDateFormat("HH:mm").format(new Date());
        return new OutputMessage(message.getContent(), message.getFrom(), time);
    }

}