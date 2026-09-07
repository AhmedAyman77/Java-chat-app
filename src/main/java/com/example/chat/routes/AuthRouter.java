package com.example.chat.routes;

import com.example.chat.handlers.AuthHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
public class AuthRouter {
    @Bean
    RouterFunction<ServerResponse> authRoute(AuthHandler handler) {
        return RouterFunctions.route()
                .POST("/users/register", handler::register)
                .POST("/users/login", handler::login)
                .GET("/users/verify", handler::verify)
                .GET("/users/{userId}", handler::getUser)
                .PUT("/users/{userId}", handler::updateUser)
                .build();
    }
}
