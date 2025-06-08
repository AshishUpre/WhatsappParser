package com.ashupre.whatsappparser.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "media_message_metadata")
public class MediaMessageMetadata {

    @Id
    private String id;
    private String s3BucketName;
    private String s3ObjectKey;
}
