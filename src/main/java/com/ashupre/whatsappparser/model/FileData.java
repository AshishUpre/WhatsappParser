package com.ashupre.whatsappparser.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "file_data")
public class FileData {
    @Id
    private String id;
    private String fileName;
    private String fileId;
    private String uploadedByUserId;
    private long size;
    private String uploadTime;

    // Getters & Setters
}
