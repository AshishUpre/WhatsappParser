package com.ashupre.whatsappparser.controller;

import com.ashupre.whatsappparser.model.ChatCursor;
import com.ashupre.whatsappparser.dto.ChatResponsePaginated;
import com.ashupre.whatsappparser.security.AESUtil;
import com.ashupre.whatsappparser.service.ChatService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/chats")
public class ChatController {

    private final ChatService chatService;

    private final AESUtil aesUtil;

    private final ObjectMapper mapper;

    // todo : fine tune this and what it calls
    @GetMapping("/{fileDriveId}/{userId}/cursor={cursor}")
    public ChatResponsePaginated getPaginatedChats(@PathVariable String fileDriveId, @PathVariable String userId,
                                                   @PathVariable String cursor) throws JsonProcessingException {
        ChatCursor prevCursor;
        System.out.println("cursor: " + cursor);

        if (cursor == null || cursor.isEmpty()) {
            prevCursor = null;
        } else {
            cursor = aesUtil.decrypt(cursor);
            System.out.println("cursor after decryption : " + cursor);
            prevCursor = mapper.readValue(cursor, ChatCursor.class);
        }

        ChatResponsePaginated response = chatService.getPaginatedChats(userId, fileDriveId, prevCursor);
        System.out.println("cursor before enc : " + response.getCursor());
        response.setCursor(aesUtil.encrypt(response.getCursor()));
        System.out.println("sending cursor as : " + response.getCursor());
        return response;
    }
}
