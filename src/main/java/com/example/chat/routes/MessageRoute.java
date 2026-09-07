package com.example.chat.routes;

import com.example.chat.handlers.MessageHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
public class MessageRoute {
    @Bean
    RouterFunction<ServerResponse> messageRoutes(MessageHandler handler) {
        return RouterFunctions.route()
                .GET("/chats/{userId}", handler::getHistory)
                .POST("/chats/messages", handler::sendMessage)
                .build();
    }
}
