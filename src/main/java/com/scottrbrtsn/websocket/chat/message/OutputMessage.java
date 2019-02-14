package com.scottrbrtsn.websocket.chat.message;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;

@Data
@AllArgsConstructor
public class OutputMessage {

    private Map<String, Object> content;
    private String from;
    private String time;

}
