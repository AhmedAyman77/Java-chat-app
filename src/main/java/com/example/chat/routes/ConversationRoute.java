package com.example.chat.routes;

import com.example.chat.handlers.ConversationHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
public class ConversationRoute {
    @Bean
    RouterFunction<ServerResponse> conversationRoutes(ConversationHandler handler) {
        return RouterFunctions.route()
                .GET("/chats", handler::getConversations)
                .DELETE("/chats/{userId}", handler::deleteConversation)
                .build();
    }
}
