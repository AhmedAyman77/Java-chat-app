package com.example.chat.services;

import com.example.chat.dtos.ConversationResponse;
import com.example.chat.models.Conversations;
import com.example.chat.repositories.ConversationRepository;
import com.example.chat.utils.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ConversationService {
    private final ConversationRepository repository;

    public Flux<ConversationResponse> getUserConversations(String userId) {
        return repository.findByParticipantsContainingOrderByLastUpdatedDesc(userId)
                .map(this::toResponse);
    }

    public Mono<Void> deleteConversation(String userId, String otherUserId) {
        return repository.findBetweenUsers(userId, otherUserId)
                .switchIfEmpty(Mono.error(CustomException.resourceNotFound("Conversation not found")))
                .flatMap(repository::delete);
    }

    public Mono<Conversations> findOrCreate(String userId, String otherUserId) {
        return repository.findBetweenUsers(userId, otherUserId)
                .switchIfEmpty(Mono.defer(() -> repository.save(
                        Conversations.builder()
                                .participants(List.of(userId, otherUserId))
                                .lastMessage("")
                                .lastUpdated(LocalDateTime.now())
                                .build()
                )));
    }

    public Mono<Conversations> updateLastMessage(String userId, String otherUserId, String content) {
        return findOrCreate(userId, otherUserId)
                .flatMap(conversation -> {
                    conversation.setLastMessage(content);
                    conversation.setLastUpdated(LocalDateTime.now());
                    return repository.save(conversation);
                });
    }

    private ConversationResponse toResponse(Conversations conversation) {
        return new ConversationResponse(
                conversation.getId(),
                conversation.getParticipants(),
                conversation.getLastMessage(),
                conversation.getLastUpdated()
        );
    }
}
