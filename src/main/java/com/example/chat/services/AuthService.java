package com.example.chat.services;

import com.example.chat.dtos.LoginRequest;
import com.example.chat.dtos.LoginResponse;
import com.example.chat.dtos.RegisterRequest;
import com.example.chat.dtos.UpdateUserRequest;
import com.example.chat.dtos.UserResponse;
import com.example.chat.models.Users;
import com.example.chat.repositories.UserRepository;
import com.example.chat.security.JwtService;
import com.example.chat.utils.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final EmailService emailService;

    public Mono<UserResponse> register(RegisterRequest req) {
        return userRepository.findByEmail(req.email())
                .hasElement()
                .flatMap(emailExists -> {
                    if (emailExists) {
                        return Mono.error(CustomException.conflict("Email already exists"));
                    }
                    return userRepository.findByUsername(req.username()).hasElement();
                })
                .flatMap(usernameExists -> {
                    if (usernameExists) {
                        return Mono.error(CustomException.conflict("Username already exists"));
                    }

                    String token = UUID.randomUUID().toString();

                    Users user = Users.builder()
                            .username(req.username())
                            .email(req.email())
                            .password(passwordEncoder.encode(req.password()))
                            .status(Users.UserStatus.offline)
                            .emailVerified(false)
                            .verificationToken(token)
                            .verificationTokenExpiresAt(LocalDateTime.now().plusHours(24))
                            .build();

                    return userRepository.save(user)
                            .flatMap(saved -> emailService
                                    .sendVerificationEmail(saved.getEmail(), token)
                                    .thenReturn(toUserResponse(saved)));
                });
    }

    public Mono<LoginResponse> login(LoginRequest req) {
        return userRepository.findByEmail(req.email())
                .switchIfEmpty(Mono.error(CustomException.badCredentials()))
                .flatMap(user -> {
                    if (!passwordEncoder.matches(req.password(), user.getPassword())) {
                        return Mono.error(CustomException.badCredentials());
                    }

                    if (!user.isEmailVerified()) {
                        return Mono.error(CustomException.badRequest("Please verify your email first"));
                    }

                    user.setStatus(Users.UserStatus.online);

                    return userRepository.save(user)
                            .map(saved -> new LoginResponse(
                                    jwtService.generateToken(saved.getId()),
                                    toUserResponse(saved)
                            ));
                });
    }

    public Mono<UserResponse> verifyEmail(String token) {
        return userRepository.findByVerificationToken(token)
                .switchIfEmpty(Mono.error(CustomException.badRequest("Invalid verification token")))
                .flatMap(user -> {
                    if (user.getVerificationTokenExpiresAt() == null ||
                            user.getVerificationTokenExpiresAt().isBefore(LocalDateTime.now())) {
                        return Mono.error(CustomException.badRequest("Verification token has expired"));
                    }

                    user.setEmailVerified(true);
                    user.setVerificationToken(null);
                    user.setVerificationTokenExpiresAt(null);

                    return userRepository.save(user).map(this::toUserResponse);
                });
    }

    public Mono<UserResponse> getUser(String userId) {
        return userRepository.findById(userId)
                .switchIfEmpty(Mono.error(CustomException.resourceNotFound("User not found")))
                .map(this::toUserResponse);
    }

    public Mono<UserResponse> updateUser(String userId, UpdateUserRequest req) {
        return userRepository.findById(userId)
                .switchIfEmpty(Mono.error(CustomException.resourceNotFound("User not found")))
                .flatMap(user -> {
                    Mono<Void> usernameCheck = Mono.empty();

                    if (req.username() != null && !req.username().equals(user.getUsername())) {
                        usernameCheck = userRepository.findByUsername(req.username())
                                .hasElement()
                                .flatMap(exists -> exists
                                        ? Mono.error(CustomException.conflict("Username already exists"))
                                        : Mono.empty());
                    }

                    return usernameCheck.then(Mono.defer(() -> {
                        if (req.username() != null) {
                            user.setUsername(req.username());
                        }
                        if (req.profilePicture() != null) {
                            user.setProfilePicture(req.profilePicture());
                        }
                        return userRepository.save(user);
                    }));
                })
                .map(this::toUserResponse);
    }

    public Mono<Void> setOffline(String userId) {
        return userRepository.findById(userId)
                .flatMap(user -> {
                    user.setStatus(Users.UserStatus.offline);
                    return userRepository.save(user);
                })
                .then();
    }

    private UserResponse toUserResponse(Users user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getProfilePicture(),
                user.getStatus(),
                user.isEmailVerified()
        );
    }
}
