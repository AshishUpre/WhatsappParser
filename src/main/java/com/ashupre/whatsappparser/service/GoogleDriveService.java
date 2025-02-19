package com.ashupre.whatsappparser.service;

import com.google.api.client.http.FileContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GoogleDriveService {

    private final Drive drive;

    private final String folderId;

    // Upload file to Google Drive folder
    public String[] uploadFile(String filePath) throws IOException {
        File fileMetadata = new File();
        fileMetadata.setName(Paths.get(filePath).getFileName().toString());
        fileMetadata.setParents(Collections.singletonList(folderId)); // Target folder ID

        FileContent mediaContent = new FileContent("application/octet-stream", new java.io.File(filePath));
        File uploadedFile = drive.files().create(fileMetadata, mediaContent)
                .setFields("id, name, parents")
                .execute();
        return new String[] {uploadedFile.getName(), uploadedFile.getId()};
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
