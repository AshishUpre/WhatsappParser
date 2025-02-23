package com.ashupre.whatsappparser.controller;

import com.ashupre.whatsappparser.model.DriveFileMetadata;
import com.ashupre.whatsappparser.repository.UserRepository;
import com.ashupre.whatsappparser.security.AESUtil;
import com.ashupre.whatsappparser.service.ChatService;
import com.ashupre.whatsappparser.service.FileDataService;
import com.ashupre.whatsappparser.service.GoogleDriveService;
import com.ashupre.whatsappparser.service.UserService;
import com.ashupre.whatsappparser.util.CookieUtil;
import com.ashupre.whatsappparser.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/drive")
@RequiredArgsConstructor
public class GoogleDriveController {
    private final GoogleDriveService googleDriveService;

    private final UserService userService;

    private final ChatService chatService;

    private final FileDataService fileDataService;

    private final AESUtil aesUtil;
    private final UserRepository userRepository;

    @PostMapping("/upload")
    public String uploadFile(@RequestParam("file") MultipartFile file, HttpServletRequest request) {
        try {
            System.out.println("Received file: " + file.getOriginalFilename());
            String jwt = CookieUtil.getDecryptedCookieValue(request, "jwt", aesUtil);
            String email = JwtUtil.extractEmail(jwt);
            System.out.println("got email: " + email);
            String userId = userService.getUserByEmail(email).getId();

            // file handling
            // /tmp/filename absolute path for the file (get by convFile.getAbsolutePath())r
            File convFile = new File(System.getProperty("java.io.tmpdir") + "/" + file.getOriginalFilename());
            try (FileOutputStream fos = new FileOutputStream(convFile)) {
                fos.write(file.getBytes());
            }

            // Upload file to Google Drive
            DriveFileMetadata metadata = googleDriveService.uploadFile(convFile.getAbsolutePath());

            System.out.println("filename : " + metadata.getFileName());
            System.out.println("fileId : " + metadata.getDriveId());

            // todo: store in DB first
            fileDataService.saveFileData(metadata, userId);
            userService.addFile(userId, metadata.getFileName(), metadata.getDriveId());

            chatService.addChatsFromFile(file, userId, metadata.getDriveId());
            return "File uploaded successfully";
        } catch (IOException e) {
            return "Error uploading file: " + e.getMessage();
        }
    }

    @GetMapping("/files/{folderId}")
    public ResponseEntity<List<String>> getAllFilesByFolderId(@PathVariable String folderId) {
        System.out.println("Reached controller for folderId: " + folderId);
        try {
            List<String> files = googleDriveService.getAllFilesInFolder(folderId);
            return ResponseEntity.ok(files);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body(List.of("Error retrieving files: " + e.getMessage()));
        }
    }


}
