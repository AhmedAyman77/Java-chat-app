package com.example.chat.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @Size(min = 6, message = "username must be more than 5 letters")
        String username,

        @Email(message = "email must be valid")
        String email,

        @Size(min=6, message = "password must be more than 5 letters")
        String password
) {}
