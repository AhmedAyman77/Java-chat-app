package com.example.chat.handlers;


import com.example.chat.dtos.RegisterRequest;
import com.example.chat.services.AuthService;
import com.example.chat.utils.CustomException;
import com.example.chat.utils.GlobalResponse;
import org.springframework.validation.Errors;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
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

//    private methods
    private Mono<RegisterRequest> validate(RegisterRequest request) {
        Errors errors = new BeanPropertyBindingResult(request, RegisterRequest.class.getName());

        validator.validate(request, errors);

        if (errors.hasErrors()) {

            String message = errors.getFieldErrors()
                    .getFirst()
                    .getDefaultMessage();

            return Mono.error(
                    CustomException.badRequest(message)
            );
        }

        return Mono.just(request);
    }
}
