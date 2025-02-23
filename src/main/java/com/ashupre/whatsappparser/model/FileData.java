package com.ashupre.whatsappparser.model;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "file_data")
@Data
@Builder
public class FileData {
    @Id
    private String id;
    private String fileName;
    private String gdriveId;
    private String userId;
    private long size;
    private Instant uploadTime;
}
