package com.scottrbrtsn.websocket.chat.message;

import com.coxautodev.graphql.tools.GraphQLQueryResolver;
import org.springframework.stereotype.Component;

@Component
class Query implements GraphQLQueryResolver {

    public String hello() {
        return "Hello world!";
    }

}
