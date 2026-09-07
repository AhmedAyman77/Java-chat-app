package com.example.chat.dtos;

import jakarta.validation.constraints.NotBlank;

public record SendMessageRequest(
        @NotBlank(message = "receiverId is required")
        String receiverId,

        @NotBlank(message = "content is required")
        String content
) {}
