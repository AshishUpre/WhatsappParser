package com.ashupre.whatsappparser.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.IndexDirection;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "chats")
@Data
public class Chat {

    @Id
    private String id;
    private String userId;
    private String sender;
    private String message;
    private String fileName;
    private String fileDbId;
    private String fileDriveId;

    // NOTE: this index creation only happens when the application starts
    // => {if you delete chats collection and re-upload files, index will not be created}
    @Indexed(direction = IndexDirection.DESCENDING)
    private Instant timestamp;

}
