package com.example.chat.utils;

import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebExceptionHandler;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
@Order(-2)
@RequiredArgsConstructor
public class GlobalExceptionHandler implements WebExceptionHandler {

    private final tools.jackson.databind.ObjectMapper objectMapper;

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {

        int statusCode = 500;
        String message = "Internal Server Error";

        if (ex instanceof CustomException customException) {
            statusCode = customException.getStatusCode();
            message = customException.getMessage();
        }

        exchange.getResponse().setStatusCode(
                org.springframework.http.HttpStatus.valueOf(statusCode)
        );
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

        GlobalResponse<?> response = new GlobalResponse<>(
                List.of(new GlobalResponse.ErrorItem(message))
        );

        try {
            byte[] bytes = objectMapper.writeValueAsBytes(response);

            DataBuffer buffer = exchange.getResponse()
                    .bufferFactory()
                    .wrap(bytes);

            return exchange.getResponse().writeWith(Mono.just(buffer));

        } catch (Exception e) {
            return Mono.error(e);
        }
    }
}