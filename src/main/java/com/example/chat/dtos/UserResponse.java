package com.example.chat.dtos;

import com.example.chat.models.Users;

public record UserResponse(
        String id,
        String username,
        String email,
        String profilePicture,
        Users.UserStatus status,
        boolean emailVerified
) {}
