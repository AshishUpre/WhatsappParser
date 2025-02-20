package com.ashupre.whatsappparser.controller;

import com.ashupre.whatsappparser.dto.ChatCursor;
import com.ashupre.whatsappparser.dto.ChatCursorWrapper;
import com.ashupre.whatsappparser.dto.ChatResponsePaginated;
import com.ashupre.whatsappparser.security.AESUtil;
import com.ashupre.whatsappparser.service.ChatService;
import com.ashupre.whatsappparser.util.CookieUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/chats")
public class ChatController {

    private final ChatService chatService;

    private final AESUtil aesUtil;

    // todo : fine tune this and what it calls
    @PostMapping("/{fileDriveId}/{userId}")
    public ChatResponsePaginated getPaginatedChats(HttpServletRequest request, /*@PathVariable String fileDriveId*/
                                                   @PathVariable String fileDriveId, @PathVariable String userId,
                                                   /*@RequestBody ChatCursor prevCursor*/
                                                   @RequestBody ChatCursorWrapper body) {
//        String userId = CookieUtil.getDecryptedCookieValue(request, "userId", aesUtil);
        ChatCursor prevCursor = body.getPrevCursor();
        System.out.println("prevcursor: " + prevCursor);

        if ( prevCursor == null || prevCursor.getTimestamp() == null && prevCursor.getId() == null) {
            prevCursor = null;
        }
        return chatService.getPaginatedChats(userId, fileDriveId, prevCursor);
    }
}
