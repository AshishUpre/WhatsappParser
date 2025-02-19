package com.ashupre.whatsappparser.controller;

import com.ashupre.whatsappparser.security.AESUtil;
import com.ashupre.whatsappparser.service.ChatsService;
import com.ashupre.whatsappparser.service.GoogleDriveService;
import com.ashupre.whatsappparser.service.UserService;
import com.ashupre.whatsappparser.util.CookieUtil;
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
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class GoogleDriveController {
    private final GoogleDriveService googleDriveService;

    private final UserService userService;

    private final ChatsService chatsService;

    private final AESUtil aesUtil;

    @PostMapping("/upload")
    public String uploadFile(@RequestParam("file") MultipartFile file, HttpServletRequest request) {
        try {
            System.out.println("Received file: " + file.getOriginalFilename());
            String email = CookieUtil.getDecryptedCookieValue(request, "email", aesUtil);
            String userId = CookieUtil.getDecryptedCookieValue(request, "userId", aesUtil);

            System.out.println("got email: " + email);
            System.out.println("got userId: " + userId);

            // file handling
            // /tmp/filename absolute path for the file (get by convFile.getAbsolutePath())
            File convFile = new File(System.getProperty("java.io.tmpdir") + "/" + file.getOriginalFilename());
            try (FileOutputStream fos = new FileOutputStream(convFile)) {
                fos.write(file.getBytes());
            }

            // Upload file to Google Drive
            String [] fileNameDriveId = googleDriveService.uploadFile(convFile.getAbsolutePath());

            System.out.println("filename : " + fileNameDriveId[0]);
            System.out.println("fileId : " + fileNameDriveId[1]);
            userService.addFile(userId, fileNameDriveId[0], fileNameDriveId[1]);

            // todo: put the chats into table from the file ============================================================
            chatsService.addChatsFromFile(file);
            return "File uploaded successfully";
        } catch (IOException e) {
            return "Error uploading file: " + e.getMessage();
        }
    }

    @GetMapping("/api/drive/files/{folderId}")
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
