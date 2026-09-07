package com.example.chat.handlers;

import com.example.chat.dtos.LoginRequest;
import com.example.chat.dtos.RegisterRequest;
import com.example.chat.dtos.UpdateUserRequest;
import com.example.chat.services.AuthService;
import com.example.chat.utils.CustomException;
import com.example.chat.utils.GlobalResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class AuthHandler {
    private final AuthService authService;
    private final Validator validator;

    public Mono<ServerResponse> register(ServerRequest request) {
        return request.bodyToMono(RegisterRequest.class)
                .flatMap(this::validate)
                .flatMap(authService::register)
                .flatMap(user -> ServerResponse.ok().bodyValue(new GlobalResponse<>(user)));
    }

    public Mono<ServerResponse> login(ServerRequest request) {
        return request.bodyToMono(LoginRequest.class)
                .flatMap(this::validate)
                .flatMap(authService::login)
                .flatMap(data -> ServerResponse.ok().bodyValue(new GlobalResponse<>(data)));
    }

    public Mono<ServerResponse> verify(ServerRequest request) {
        return authService.verifyEmail(request.queryParam("token")
                        .orElseThrow(() -> CustomException.badRequest("Verification token is required")))
                .flatMap(user -> ServerResponse.ok().bodyValue(new GlobalResponse<>(user)));
    }

    public Mono<ServerResponse> getUser(ServerRequest request) {
        return authService.getUser(request.pathVariable("userId"))
                .flatMap(user -> ServerResponse.ok().bodyValue(new GlobalResponse<>(user)));
    }

    public Mono<ServerResponse> updateUser(ServerRequest request) {
        return request.bodyToMono(UpdateUserRequest.class)
                .flatMap(this::validate)
                .flatMap(body -> currentUser(request)
                        .flatMap(userId -> authService.updateUser(userId, body)))
                .flatMap(user -> ServerResponse.ok().bodyValue(new GlobalResponse<>(user)));
    }

    private Mono<String> currentUser(ServerRequest request) {
        return request.principal().map(java.security.Principal::getName);
    }

    private <T> Mono<T> validate(T request) {
        Errors errors = new BeanPropertyBindingResult(request, request.getClass().getName());
        validator.validate(request, errors);

        if (errors.hasErrors()) {
            return Mono.error(CustomException.badRequest(
                    errors.getFieldErrors().getFirst().getDefaultMessage()));
        }

        return Mono.just(request);
    }
}
