package com.example.chat.services;

import com.example.chat.dtos.MessageResponse;
import com.example.chat.dtos.SendMessageRequest;
import com.example.chat.models.Messages;
import com.example.chat.repositories.MessageRepository;
import com.example.chat.repositories.UserRepository;
import com.example.chat.utils.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class MessageService {
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final ConversationService conversationService;

    public Mono<MessageResponse> send(String senderId, SendMessageRequest request) {
        if (senderId.equals(request.receiverId())) {
            return Mono.error(CustomException.badRequest("You cannot send a message to yourself"));
        }

        return userRepository.findById(request.receiverId())
                .switchIfEmpty(Mono.error(CustomException.resourceNotFound("Receiver not found")))
                .flatMap(receiver -> {
                    Messages message = Messages.builder()
                            .senderId(senderId)
                            .receiverId(request.receiverId())
                            .content(request.content().trim())
                            .status(Messages.MessageStatus.SENT)
                            .createdAt(LocalDateTime.now())
                            .build();

                    return messageRepository.save(message)
                            .flatMap(saved -> conversationService
                                    .updateLastMessage(senderId, request.receiverId(), saved.getContent())
                                    .thenReturn(toResponse(saved)));
                });
    }

    public Flux<MessageResponse> getHistory(String userId, String otherUserId) {
        return userRepository.findById(otherUserId)
                .switchIfEmpty(Mono.error(CustomException.resourceNotFound("User not found")))
                .thenMany(messageRepository
                        .findBySenderIdAndReceiverIdOrSenderIdAndReceiverIdOrderByCreatedAtAsc(
                                userId, otherUserId, otherUserId, userId))
                .map(this::toResponse);
    }

    public Mono<MessageResponse> markRead(String userId, String messageId) {
        return messageRepository.findById(messageId)
                .switchIfEmpty(Mono.error(CustomException.resourceNotFound("Message not found")))
                .flatMap(message -> {
                    if (!message.getReceiverId().equals(userId)) {
                        return Mono.error(CustomException.badRequest("You cannot read this message"));
                    }

                    message.setStatus(Messages.MessageStatus.READ);
                    message.setReadAt(LocalDateTime.now());

                    return messageRepository.save(message);
                })
                .map(this::toResponse);
    }

    private MessageResponse toResponse(Messages message) {
        return new MessageResponse(
                message.getId(),
                message.getSenderId(),
                message.getReceiverId(),
                message.getContent(),
                message.getStatus(),
                message.getCreatedAt(),
                message.getReadAt()
        );
    }
}
