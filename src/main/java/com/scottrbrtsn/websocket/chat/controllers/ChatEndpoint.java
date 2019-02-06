package com.scottrbrtsn.websocket.chat.controllers;


import com.scottrbrtsn.websocket.chat.message.OutputMessage;
import com.scottrbrtsn.websocket.chat.message.Message;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.text.SimpleDateFormat;
import java.util.Date;


@Controller
@RequestMapping(value = "/chat/{username}")
public class ChatEndpoint {

    @MessageMapping("/chat")
    @SendTo("/topic/messages")
    public OutputMessage send(final Message message) {

        final String time = new SimpleDateFormat("HH:mm").format(new Date());
        return new OutputMessage(message.getFrom(), message.getContent(), time);
    }

}