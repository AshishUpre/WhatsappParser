package com.ashupre.whatsappparser.dto;

import com.ashupre.whatsappparser.model.Chat;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class ChatResponsePaginated {

    private List<ChatDTO> chatList;
    private String cursor;
}
