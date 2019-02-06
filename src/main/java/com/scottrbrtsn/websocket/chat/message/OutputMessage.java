package com.scottrbrtsn.websocket.chat.message;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class OutputMessage {

    private String from;
    private String content;
    private String time;

}
