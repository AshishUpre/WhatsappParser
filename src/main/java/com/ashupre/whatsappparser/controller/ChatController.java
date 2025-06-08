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

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/chats")
@Slf4j
public class ChatController {

    private final ChatService chatService;

    private final AESUtil aesUtil;

    private final ObjectMapper jacksonMapper;

    private final UserRepository userRepository;

    @GetMapping("/{fileId}/cursor={cursor}")
    public ChatResponsePaginated getPaginatedChats(@PathVariable String fileId, @PathVariable String cursor,
                                                   Principal user) throws JsonProcessingException {
        ChatCursor prevCursor;
        log.debug("cursor: " + cursor);
        String userId = userRepository.findByoAuthProviderUserId(
                        OAuth2PrincipalUtil.getAttributes(user, "sub")
                ).orElseThrow(() -> new UserNotFoundException("User not found")).getId();

        if (cursor == null || cursor.isEmpty()) {
            prevCursor = null;
        } else {
            cursor = new String(Base64.getUrlDecoder().decode(cursor));
            cursor = aesUtil.decrypt(cursor);
            prevCursor = jacksonMapper.readValue(cursor, ChatCursor.class);
        }

        ChatResponsePaginated response = chatService.getPaginatedChats(userId, fileId, prevCursor);

        response.setCursor(aesUtil.encrypt(response.getCursor()));
        response.setCursor(Base64.getUrlEncoder().encodeToString(response.getCursor().getBytes()));
        log.debug("sending cursor as : {}", response.getCursor());
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            throw new RuntimeException(e + "Thread interrupted");
        }
        return response;
    }
}
