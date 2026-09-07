package com.example.chat.dtos;

import java.time.LocalDateTime;
import java.util.List;

public record ConversationResponse(
        String id,
        List<String> participants,
        String lastMessage,
        LocalDateTime lastUpdated
) {}
