package com.example.chat.services;

import com.example.chat.dtos.RegisterRequest;
import com.example.chat.dtos.UserResponse;
import com.example.chat.models.Users;
import com.example.chat.repositories.UserRepository;
import com.example.chat.utils.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public Mono<UserResponse> register(RegisterRequest req) {
        Users newUser = createUser(req);
        return validateEmail(req.email())
                .then(validateUsername(req.username()))
                .then(userRepository.save(newUser))
                .map(this::toUserResponse);
    }


//    private methods

    private Mono<Void> validateEmail(String email) {
        return userRepository.findByEmail(email)
                .hasElement()
                .flatMap(exists -> {
                    if (exists) {
                        return Mono.error(CustomException.conflict("Email already exists"));
                    }

                    return Mono.empty();
                });
    }

    private Mono<Void> validateUsername(String username) {

        return userRepository.findByUsername(username)
                .hasElement()
                .flatMap(exists -> {
                    if (exists) {
                        return Mono.error(CustomException.conflict("Username already exists"));
                    }

                    return Mono.empty();
                });
    }

    private Users createUser(RegisterRequest req) {
        Users user = new Users();

        user.setUsername(req.username());
        user.setEmail(req.email());
        user.setPassword(passwordEncoder.encode(req.password()));

        return user;
    }

    private UserResponse toUserResponse(Users user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail()
        );
    }
}
