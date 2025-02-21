package com.ashupre.whatsappparser.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.IndexDirection;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

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

    // convert to utc before storing in db
    public static Instant localToUTC(LocalDateTime localTimestamp, ZoneId zoneId) {
        return localTimestamp.atZone(zoneId).toInstant();
    }

    // convert to local after getting back
    public static LocalDateTime utcToLocal(Instant timestamp, ZoneId zoneId) {
        return timestamp.atZone(zoneId).toLocalDateTime();
    }
}
