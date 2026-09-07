package com.example.chat.dtos;

public record LoginResponse(
        String token,
        UserResponse user
) {}
