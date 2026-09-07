package com.example.chat.dtos;

import com.example.chat.models.Messages;

import java.time.LocalDateTime;

public record MessageResponse(
        String id,
        String senderId,
        String receiverId,
        String content,
        Messages.MessageStatus status,
        LocalDateTime createdAt,
        LocalDateTime readAt
) {}
