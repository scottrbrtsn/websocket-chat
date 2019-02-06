package com.scottrbrtsn.websocket.chat.message;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;

import java.text.SimpleDateFormat;
import java.util.Date;

@Data
@AllArgsConstructor
public class OutputMessage {

    private String from;
    private String content;
    private String time;

    @MessageMapping("/chat")
    @SendTo("/topic/messages")
    public OutputMessage send(Message message) throws Exception {
        String time = new SimpleDateFormat("HH:mm").format(new Date());
        return new OutputMessage(message.getFrom(), message.getContent(), time);
    }

}
