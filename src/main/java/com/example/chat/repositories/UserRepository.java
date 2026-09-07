package com.example.chat.repositories;

import com.example.chat.models.Users;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

public interface UserRepository extends ReactiveMongoRepository<Users, String> {
    Mono<Users> findByUsername(String username);
    Mono<Users> findByEmail(String email);
    Mono<Users> findByVerificationToken(String verificationToken);
}
