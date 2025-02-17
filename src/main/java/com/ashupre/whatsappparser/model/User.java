package com.ashupre.whatsappparser.model;

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
    private List<String> fileIds; // References uploaded files

}
