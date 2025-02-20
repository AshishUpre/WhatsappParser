package com.ashupre.whatsappparser.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;

@Data
@AllArgsConstructor
public class ChatCursor {

    private Instant timestamp;
    private String id;
}
