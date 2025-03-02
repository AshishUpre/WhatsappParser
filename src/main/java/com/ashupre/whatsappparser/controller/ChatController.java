package com.ashupre.whatsappparser.controller;

import com.ashupre.whatsappparser.exceptions.UserNotFoundException;
import com.ashupre.whatsappparser.model.ChatCursor;
import com.ashupre.whatsappparser.dto.ChatResponsePaginated;
import com.ashupre.whatsappparser.repository.UserRepository;
import com.ashupre.whatsappparser.security.AESUtil;
import com.ashupre.whatsappparser.service.ChatService;
import com.ashupre.whatsappparser.util.OAuth2PrincipalUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;

import java.security.Principal;
import java.util.Base64;

import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/chats")
public class ChatController {

    private final ChatService chatService;

    private final AESUtil aesUtil;

    private final ObjectMapper jacksonMapper;

    private final UserRepository userRepository;

    @GetMapping("/{fileDriveId}/cursor={cursor}")
    public ChatResponsePaginated getPaginatedChats(@PathVariable String fileDriveId, @PathVariable String cursor,
                                                   Principal user) throws JsonProcessingException {
        ChatCursor prevCursor;
        System.out.println("cursor: " + cursor);
        String userId = userRepository.findByProviderId(
                        OAuth2PrincipalUtil.getAttributes(user, "sub")
                ).orElseThrow(() -> new UserNotFoundException("User not found")).getId();

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

        response.setCursor(aesUtil.encrypt(response.getCursor()));
        response.setCursor(Base64.getUrlEncoder().encodeToString(response.getCursor().getBytes()));
        System.out.println("sending cursor as : " + response.getCursor());
        try {
            Thread.sleep(1200);
        } catch (InterruptedException e) {
            throw new RuntimeException(e + "Thread interrupted");
        }
        return response;
    }
}
