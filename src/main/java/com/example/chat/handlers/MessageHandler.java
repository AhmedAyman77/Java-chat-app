package com.example.chat.handlers;

import com.example.chat.dtos.SendMessageRequest;
import com.example.chat.services.MessageService;
import com.example.chat.utils.CustomException;
import com.example.chat.utils.GlobalResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class MessageHandler {
    private final MessageService messageService;
    private final Validator validator;

    public Mono<ServerResponse> getHistory(ServerRequest request) {
        return request.principal()
                .map(java.security.Principal::getName)
                .flatMapMany(userId -> messageService.getHistory(
                        userId, request.pathVariable("userId")))
                .collectList()
                .flatMap(data -> ServerResponse.ok().bodyValue(new GlobalResponse<>(data)));
    }

    public Mono<ServerResponse> sendMessage(ServerRequest request) {
        return request.bodyToMono(SendMessageRequest.class)
                .flatMap(this::validate)
                .flatMap(body -> request.principal()
                        .map(java.security.Principal::getName)
                        .flatMap(userId -> messageService.send(userId, body)))
                .flatMap(data -> ServerResponse.ok().bodyValue(new GlobalResponse<>(data)));
    }

    private Mono<SendMessageRequest> validate(SendMessageRequest request) {
        Errors errors = new BeanPropertyBindingResult(request, SendMessageRequest.class.getName());
        validator.validate(request, errors);

        if (errors.hasErrors()) {
            return Mono.error(CustomException.badRequest(
                    errors.getFieldErrors().getFirst().getDefaultMessage()));
        }

        return Mono.just(request);
    }
}
