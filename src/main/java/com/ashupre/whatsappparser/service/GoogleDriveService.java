package com.ashupre.whatsappparser.service;

import com.ashupre.whatsappparser.model.DriveFileMetadata;
import com.google.api.client.http.FileContent;
import com.google.api.client.util.DateTime;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class GoogleDriveService {

    private final Drive drive;

    private final String folderId;

    // Upload file to Google Drive folder
    public DriveFileMetadata uploadFile(String filePath) throws IOException {
        File fileMetadata = new File();
        fileMetadata.setName(Paths.get(filePath).getFileName().toString());
        fileMetadata.setParents(Collections.singletonList(folderId)); // Target folder ID

        FileContent mediaContent = new FileContent("application/octet-stream", new java.io.File(filePath));
        File uploadedFile = drive.files().create(fileMetadata, mediaContent)
                .setFields("id, name, parents, size, createdTime")
                // only these fields will be set in uploadedFile, and can get them using get*(), rest null
                .execute();
        log.info("uploadedFile: " + uploadedFile);
        LocalDateTime now = LocalDateTime.now();
        return new DriveFileMetadata(uploadedFile.getId(), uploadedFile.getName(), uploadedFile.getSize(),
                now);
    }

    public List<String> getAllFilesInFolder(String folderId) throws IOException {
        List<String> fileNames = new ArrayList<>();

        String query = String.format("'%s' in parents and mimeType != 'application/vnd.google-apps.folder'", folderId);

        FileList result = drive.files().list()
                .setQ(query)
                .setFields("files(id, name, size, createdTime)")
                .execute();

        for (File file : result.getFiles()) {
            fileNames.add(String.format("%s (ID: %s) - %s KB",
                    file.getName(), file.getId(), file.getSize() / 1024));
        }

        return fileNames;
    }
}
