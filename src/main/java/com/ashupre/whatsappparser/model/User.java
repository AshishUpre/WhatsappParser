package com.ashupre.whatsappparser.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.List;

@Document(collection = "users")
@Data
@Builder
public class User {

    @Id
    private String id;
    private String name;
    // the oauth provider e.g., "google", "github"
    private String provider;
    // id from oauth provider
    private String providerId;
    private String email;
    private String profilePic;
    private List<FileMetadata> files;


    @Data
    @AllArgsConstructor
    public static class FileMetadata {
        private String fileName;
        private String driveId; // References uploaded files
        private String fileId;
    }

    public boolean hasFileDriveId(String driveId) {
        return files != null && files.stream().anyMatch(file -> file.getDriveId().equals(driveId));
    }
}
