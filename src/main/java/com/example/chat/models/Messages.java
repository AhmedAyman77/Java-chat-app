package com.example.chat.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;
import java.lang.String;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document(collation = "Messages")
public class Messages {
    @Id
    private String id;

    @Field(name = "sender_id")
    private String senderId;

    @Field(name = "receiver_id")
    private String receiverId;

    @Field(name = "content")
    private String content;

    @CreatedDate
    private LocalDateTime createdDate;

    @LastModifiedDate
    private LocalDateTime lastModifiedDate;
}
