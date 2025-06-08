package com.ashupre.whatsappparser.controller;

import com.ashupre.whatsappparser.model.FileData;
import com.ashupre.whatsappparser.service.*;
import com.ashupre.whatsappparser.util.OAuth2PrincipalUtil;
import com.google.api.client.http.HttpStatusCodes;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.security.Principal;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
@Slf4j
public class FileController {
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

        FileData fileData = fileDataService.saveFileData(convFile, userId);
        userService.addFile(userId, convFile.getName(), fileData.getId());

        chatService.addChatsFromFile(file, userId, fileData.getId());
        return ResponseEntity.ok("File uploaded successfully");
    }

    @DeleteMapping("/{fileId}")
    public ResponseEntity<String> deleteFile(Principal user, @PathVariable String fileId) {
        String email = OAuth2PrincipalUtil.getAttributes(user, "email");
        System.out.println("Deleting file: " + fileId + " " + email);
        deletionService.deleteFile(fileId, email);
        return ResponseEntity.status(HttpStatusCodes.STATUS_CODE_OK).body("Deleted successfully");
    }

}
