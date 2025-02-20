package com.ashupre.whatsappparser.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "file_data")
@Data
public class FileData {
    @Id
    private String id;
    private String fileName;
    private String gdriveId;
    private String userId;
    private long size;
    private String uploadTime;
}
