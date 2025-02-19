package com.ashupre.whatsappparser.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "chats")
@Data
public class Chat {
    @Id
    private Long id;
    private Long userId;
    private String sender;
    private String message;
    private String fileNameId;
    private LocalDateTime timestamp;
}
