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
    private String email;
    private String name;
    // the oauth provider e.g., "google", "github"
    private String oAuthProvider;
    // id of user  from oauth provider
    private String oAuthProviderUserId;
    private String profilePic;
    private List<FileMetadata> files;

    public boolean hasFileId(String fileId) {
        return files != null && files.stream().anyMatch(file -> file.fileId().equals(fileId));
    }

}
