package com.ashupre.whatsappparser.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.List;

@Document(collection = "users")
@Data
public class User {

    @Id
    private String id;
    private String username;
    private String password;
    private String email;
    private String profilePic; // base64 string
    private List<FileMetadata> files;

    @Data
    @AllArgsConstructor
    public static class FileMetadata {
        private String fileName;
        private String driveId; // References uploaded files
    }

    public boolean hasFileDriveId(String driveId) {
        return files != null && files.stream().anyMatch(file -> file.getDriveId().equals(driveId));
    }
}
