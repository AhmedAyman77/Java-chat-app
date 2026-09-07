package com.example.chat.websocket;

import com.example.chat.dtos.ReadMessageRequest;
import com.example.chat.dtos.SendMessageRequest;
import com.example.chat.dtos.TypingRequest;
import com.example.chat.services.AuthService;
import com.example.chat.services.MessageService;
import com.example.chat.security.JwtService;
import com.example.chat.utils.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class ChatWebSocketHandler implements WebSocketHandler {
    private final JwtService jwtService;
    private final MessageService messageService;
    private final AuthService authService;
    private final Sinks.Many<ChatEvent> events = Sinks.many().multicast().onBackpressureBuffer();
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final tools.jackson.databind.ObjectMapper objectMapper = new tools.jackson.databind.ObjectMapper();

    @Override
    public Mono<Void> handle(WebSocketSession session) {
        String token = session.getHandshakeInfo().getUri().getQuery();
        String jwt = getToken(token);

        if (jwt == null || !jwtService.isValid(jwt)) {
            return session.close();
        }

        String userId = jwtService.getUserId(jwt);
        sessions.put(userId, session);

        Flux<WebSocketMessage> output = events.asFlux()
                .filter(event -> event.receiverId().equals(userId) || event.senderId().equals(userId))
                .map(event -> session.textMessage(toJson(event)));

        Mono<Void> input = session.receive()
                .map(WebSocketMessage::getPayloadAsText)
                .flatMap(message -> handleMessage(userId, message))
                .then();

        return session.send(output.mergeWith(input.cast(WebSocketMessage.class)))
                .doFinally(signal -> {
                    sessions.remove(userId);
                    authService.setOffline(userId).subscribe();
                });
    }

    private Mono<Void> handleMessage(String userId, String json) {
        try {
            ChatRequest request = objectMapper.readValue(json, ChatRequest.class);

            return switch (request.type()) {
                case "message" -> messageService.send(
                                userId,
                                new SendMessageRequest(request.receiverId(), request.content()))
                        .doOnNext(message -> events.tryEmitNext(
                                ChatEvent.message(message.senderId(), message.receiverId(), message)))
                        .then();

                case "typing" -> {
                    TypingRequest typing = new TypingRequest(request.receiverId(), request.typing());
                    events.tryEmitNext(ChatEvent.typing(userId, typing.receiverId(), typing.typing()));
                    yield Mono.empty();
                }

                case "read" -> messageService.markRead(userId, request.messageId())
                        .doOnNext(message -> events.tryEmitNext(
                                ChatEvent.read(message.senderId(), message.receiverId(), message)))
                        .then();

                default -> Mono.error(CustomException.badRequest("Unknown websocket message type"));
            };
        } catch (Exception e) {
            return Mono.error(CustomException.badRequest("Invalid websocket message"));
        }
    }

    private String getToken(String query) {
        if (query == null || query.isBlank()) {
            return null;
        }

        for (String part : query.split("&")) {
            if (part.startsWith("token=")) {
                return part.substring(6);
            }
        }

        return null;
    }

    private String toJson(ChatEvent event) {
        try {
            return objectMapper.writeValueAsString(event);
        } catch (Exception e) {
            return "{\"type\":\"error\",\"message\":\"Could not create event\"}";
        }
    }

    public record ChatRequest(
            String type,
            String receiverId,
            String content,
            boolean typing,
            String messageId
    ) {}

    public record ChatEvent(
            String type,
            String senderId,
            String receiverId,
            Object data
    ) {
        static ChatEvent message(String senderId, String receiverId, Object data) {
            return new ChatEvent("message", senderId, receiverId, data);
        }

        static ChatEvent typing(String senderId, String receiverId, boolean typing) {
            return new ChatEvent("typing", senderId, receiverId, Map.of("typing", typing));
        }

        static ChatEvent read(String senderId, String receiverId, Object data) {
            return new ChatEvent("read", senderId, receiverId, data);
        }
    }
}
