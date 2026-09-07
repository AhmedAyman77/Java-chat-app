package com.example.chat.dtos;

public record TypingRequest(
        String receiverId,
        boolean typing
) {}
