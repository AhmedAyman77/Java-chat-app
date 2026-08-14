package com.example.chat.repositories;

import com.example.chat.models.Conversations;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface ConversationRepository extends ReactiveMongoRepository<Conversations, String> {
}
