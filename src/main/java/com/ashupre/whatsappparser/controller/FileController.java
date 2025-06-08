package com.ashupre.whatsappparser.controller;

import com.ashupre.whatsappparser.service.*;
import com.ashupre.whatsappparser.util.OAuth2PrincipalUtil;
import com.google.api.client.http.HttpStatusCodes;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
        fileDataService.uploadFile(email, file);
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
