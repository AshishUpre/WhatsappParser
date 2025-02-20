package com.ashupre.whatsappparser.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class UserDTO {
    private String id;
    private String username;
    private String email;
    private String password;
    private MultipartFile file;
}
