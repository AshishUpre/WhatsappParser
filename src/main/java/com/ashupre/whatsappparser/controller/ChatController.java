package com.ashupre.whatsappparser.controller;

import com.ashupre.whatsappparser.model.ChatCursor;
import com.ashupre.whatsappparser.dto.ChatResponsePaginated;
import com.ashupre.whatsappparser.security.AESUtil;
import com.ashupre.whatsappparser.service.ChatService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;

import java.util.Base64;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/chats")
public class ChatController {

    private final ChatService chatService;

    private final AESUtil aesUtil;

    private final ObjectMapper jacksonMapper;

    // todo : fine tune this and what it calls
    @GetMapping("/{fileDriveId}/{userId}/cursor={cursor}")
    public ChatResponsePaginated getPaginatedChats(@PathVariable String fileDriveId, @PathVariable String userId,
                                                   @PathVariable String cursor) throws JsonProcessingException {
        ChatCursor prevCursor;
        System.out.println("cursor: " + cursor);

        if (cursor == null || cursor.isEmpty()) {
            prevCursor = null;
        } else {
            cursor = new String(Base64.getUrlDecoder().decode(cursor));
            cursor = aesUtil.decrypt(cursor);
            System.out.println("cursor after decryption : " + cursor);
            prevCursor = jacksonMapper.readValue(cursor, ChatCursor.class);
        }

        ChatResponsePaginated response = chatService.getPaginatedChats(userId, fileDriveId, prevCursor);
        System.out.println("cursor before enc : " + response.getCursor());

        // encrypt the cursor then convert to Base64 and send
        // WHY? because on encryption, there is a chance of characters like +, /, =, etc.
        // if / is there it will be another route and wont get routed to this route
        // but Base64 encoding doesnt have most special characters, but it has + and /
        // hence we make use of URL safe Base64 encoding that converts + and / to - and _

        response.setCursor(aesUtil.encrypt(response.getCursor()));
        response.setCursor(Base64.getUrlEncoder().encodeToString(response.getCursor().getBytes()));
        System.out.println("sending cursor as : " + response.getCursor());
        return response;
    }
}
