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
     * Deletes everything corresponding to a fileId.
     * transactional to roll back all the db operations in case of failures.
     */
    @Transactional
    public void deleteFile(String fileId, String email) {
        // first delete the file from user document
        userService.deleteFileById(email, fileId);
        // then delete all chats corresponding to that file
        chatService.deleteChats(fileId);
        // then delete the file from db
        fileDataService.deleteFileById(fileId);
    }
}
