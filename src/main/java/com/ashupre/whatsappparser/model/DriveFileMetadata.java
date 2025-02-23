package com.ashupre.whatsappparser.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@AllArgsConstructor
@Data
public class DriveFileMetadata {

    private String driveId;
    private String fileName;
    private Long size;
    private LocalDateTime uploadTime;
}
