package com.ashupre.whatsappparser.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ChatDTO {

    private String sender;
    private String message;
    private String timestamp;

}
