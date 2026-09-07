package com.example.chat.dtos;

import jakarta.validation.constraints.Size;

public record UpdateUserRequest(
        @Size(min = 3, message = "username must be at least 3 characters")
        String username,

        String profilePicture
) {}
