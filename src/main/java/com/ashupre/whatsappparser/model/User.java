package com.ashupre.whatsappparser.model;

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
    // id of user  from oauth provider
    private String providerId;
    private String email;
    private String profilePic;
    private List<FileMetadata> files;

    public record FileMetadata (String fileName,
                                String driveId, // References uploaded files
                                String fileId) {}

    public boolean hasFileDriveId(String driveId) {
        return files != null && files.stream().anyMatch(file -> file.driveId().equals(driveId));
    }

}
