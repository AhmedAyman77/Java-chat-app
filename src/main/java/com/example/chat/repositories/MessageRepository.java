package com.example.chat.repositories;

import com.example.chat.models.Messages;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

public interface MessageRepository extends ReactiveMongoRepository<Messages, String> {
    Flux<Messages> findBySenderIdAndReceiverIdOrSenderIdAndReceiverIdOrderByCreatedAtAsc(
            String senderId, String receiverId, String receiverId2, String senderId2);
}
