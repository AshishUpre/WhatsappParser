package com.ashupre.whatsappparser.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TransactionalDeletionService {

    private final UserService userService;
    private final ChatService chatService;
    private final FileDataService fileDataService;

    /**
     * Deletes everything corresponding to a fileDriveId.
     * transactional to roll back all the db operations in case of failures.
     */
    @Transactional
    public void deleteFile(String fileDriveId, String email) {
        // first delete the file from user document
        userService.deleteFileByDriveId(email, fileDriveId);
        // then delete all chats corresponding to that file
        chatService.deleteChats(fileDriveId);
        // then delete the file from db
        fileDataService.deleteFileByDriveId(fileDriveId);
    }
}
