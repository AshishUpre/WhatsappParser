package com.ashupre.whatsappparser.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class ChatResponsePaginated {

    private List<ChatDTO> chatList;
    private String cursor;
}
