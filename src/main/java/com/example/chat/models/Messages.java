package com.example.chat.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document(collection = "messages")
@CompoundIndex(name = "chat_history_index", def = "{'senderId': 1, 'receiverId': 1, 'createdAt': -1}")
public class Messages {
    @Id
    private String id;

    private String senderId;
    private String receiverId;
    private String content;
    private MessageStatus status;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime readAt;

    public enum MessageStatus {
        SENT, READ
    }
}
