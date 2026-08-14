package com.example.chat.repositories;

import com.example.chat.models.Messages;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface MessageRepository extends ReactiveMongoRepository<Messages, String> {
}
