package com.ashupre.whatsappparser.config;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.drive.Drive;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.util.Collections;

@Configuration
public class GoogleDriveConfig {

    private static final String APPLICATION_NAME = "WhatsappParser";
    private static final String CREDENTIALS_FILE_PATH = "/credentials.json";

    @Bean
    public Drive drive(HttpTransport httpTransport, JsonFactory jsonFactory) throws IOException {
        GoogleCredential credential = GoogleCredential.fromStream(
                GoogleDriveConfig.class.getResourceAsStream(CREDENTIALS_FILE_PATH)
        ).createScoped(Collections.singleton("https://www.googleapis.com/auth/drive"));

        return new Drive.Builder(httpTransport, jsonFactory, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    @Bean
    public HttpTransport httpTransport() {
        return new NetHttpTransport();
    }

    @Bean
    public JsonFactory jsonFactory() {
        return JacksonFactory.getDefaultInstance();
    }

}
