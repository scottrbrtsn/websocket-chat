package com.scottrbrtsn.websocket.chat.message;

import lombok.Data;

import java.util.Map;

@Data
public class Message {
    private String from;
    private String to;
    private Map<String, Object> content;
}