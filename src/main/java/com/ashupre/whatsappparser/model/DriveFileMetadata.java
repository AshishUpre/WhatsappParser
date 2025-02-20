package com.ashupre.whatsappparser.model;

import com.google.api.client.util.DateTime;
import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class DriveFileMetadata {

    private String driveId;
    private String fileName;
    private Long size;
    private DateTime creationTime;
}
