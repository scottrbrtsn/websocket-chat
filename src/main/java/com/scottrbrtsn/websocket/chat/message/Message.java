package com.scottrbrtsn.websocket.chat.message;

import lombok.Data;

@Data
public class Message {
    private String from;
    private String to;
    private String content;

}