package com.example.chat.repositories;

import com.example.chat.models.Conversations;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.data.mongodb.repository.Query;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ConversationRepository extends ReactiveMongoRepository<Conversations, String> {
    @Query("{ 'participants': { '$all': [ ?0, ?1 ] } }")
    Mono<Conversations> findBetweenUsers(String userId, String otherUserId);

    Flux<Conversations> findByParticipantsContainingOrderByLastUpdatedDesc(String userId);
}
