package com.ashupre.whatsappparser.controller;

import com.ashupre.whatsappparser.service.*;
import com.ashupre.whatsappparser.util.OAuth2PrincipalUtil;
import com.google.api.client.http.HttpStatusCodes;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.security.Principal;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
@Slf4j
public class FileController {

    private final FileDataService fileDataService;

    private final TransactionalDeletionService deletionService;

    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file, Principal user) {
        log.info("Received file: {}", file.getOriginalFilename());
        String email = OAuth2PrincipalUtil.getAttributes(user, "email");

        log.info("got email: {}", email);
        return fileDataService.uploadFile(email, file);
    }

    @GetMapping("/dummy")
    public ResponseEntity<InputStreamResource> dummy() throws IOException {
        File file = new File("Dummy_chat.txt");
        String contentType = Files.probeContentType(file.toPath());
        InputStreamResource resource = new InputStreamResource(new FileInputStream(file));

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + file.getName())
                .header(HttpHeaders.CONTENT_TYPE, contentType)
                .contentLength(file.length())
                .body(resource);
    }

    @DeleteMapping("/{fileId}")
    public ResponseEntity<String> deleteFile(Principal user, @PathVariable String fileId) {
        String email = OAuth2PrincipalUtil.getAttributes(user, "email");
        System.out.println("Deleting file: " + fileId + " " + email);
        deletionService.deleteFile(fileId, email);
        return ResponseEntity.status(HttpStatusCodes.STATUS_CODE_OK).body("Deleted successfully");
    }

}
