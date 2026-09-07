package com.example.chat.handlers;

import com.example.chat.services.ConversationService;
import com.example.chat.utils.GlobalResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class ConversationHandler {
    private final ConversationService conversationService;

    public Mono<ServerResponse> getConversations(ServerRequest request) {
        return request.principal()
                .map(java.security.Principal::getName)
                .flatMapMany(conversationService::getUserConversations)
                .collectList()
                .flatMap(data -> ServerResponse.ok().bodyValue(new GlobalResponse<>(data)));
    }

    public Mono<ServerResponse> deleteConversation(ServerRequest request) {
        return request.principal()
                .map(java.security.Principal::getName)
                .flatMap(userId -> conversationService.deleteConversation(
                        userId, request.pathVariable("userId")))
                .then(ServerResponse.noContent().build());
    }
}
