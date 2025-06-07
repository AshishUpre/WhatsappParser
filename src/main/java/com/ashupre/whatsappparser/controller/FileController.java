package com.ashupre.whatsappparser.controller;

import com.ashupre.whatsappparser.model.DriveFileMetadata;
import com.ashupre.whatsappparser.service.*;
import com.ashupre.whatsappparser.util.OAuth2PrincipalUtil;
import com.google.api.client.http.HttpStatusCodes;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.Principal;
import java.util.List;

@RestController
// todo: change to /api/files
@RequestMapping("/api/drive")
@RequiredArgsConstructor
@Slf4j
public class FileController {
    private final GoogleDriveService googleDriveService;

    private final ChatService chatService;

    private final FileDataService fileDataService;

    private final UserService userService;

    private final TransactionalDeletionService deletionService;

    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file, Principal user) {
        log.info("Received file: {}", file.getOriginalFilename());

        String email = OAuth2PrincipalUtil.getAttributes(user, "email");

        log.info("got email: {}", email);
        String userId = userService.getUserByEmail(email).getId();

        // file handling
        // /tmp/filename absolute path for the file (get by convFile.getAbsolutePath())
        File convFile = new File(System.getProperty("java.io.tmpdir") + "/" + file.getOriginalFilename());

        DriveFileMetadata metadata;
        try (FileOutputStream fos = new FileOutputStream(convFile)) {
            fos.write(file.getBytes());
            // Upload file to Google Drive
            metadata = googleDriveService.uploadFile(convFile.getAbsolutePath());
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body("Error uploading file: " + e.getMessage());
        }

        log.info("filename : {}", metadata.getFileName());
        log.info("fileId : {}", metadata.getDriveId());

        fileDataService.saveFileData(metadata, userId);
        userService.addFile(userId, metadata.getFileName(), metadata.getDriveId());

        chatService.addChatsFromFile(file, userId, metadata.getDriveId());
        return ResponseEntity.ok("File uploaded successfully");
    }

    @DeleteMapping("/{fileDriveId}")
    public ResponseEntity<String> deleteFile(Principal user, @PathVariable String fileDriveId) {
        String email = OAuth2PrincipalUtil.getAttributes(user, "email");
        log.info("Deleting file: " + fileDriveId + " " + email);
        deletionService.deleteFile(fileDriveId, email);
        return ResponseEntity.status(HttpStatusCodes.STATUS_CODE_OK).body("Deleted successfully");
    }

    @GetMapping("/{folderId}")
    public ResponseEntity<List<String>> getAllFilesByFolderId(@PathVariable String folderId) {
        log.info("Reached controller for folderId: {}", folderId);
        try {
            List<String> files = googleDriveService.getAllFilesInFolder(folderId);
            return ResponseEntity.ok(files);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body(List.of("Error retrieving files: " + e.getMessage()));
        }
    }

}
