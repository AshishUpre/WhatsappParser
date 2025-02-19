package com.ashupre.whatsappparser.config;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EnvConfig {

    private static final Dotenv dotenv = Dotenv.load();

    @Bean
    public String folderId() {
        return dotenv.get("GOOGLE_DRIVE_FOLDER_ID");
    }

    /**
     * key and iv must be present in .env, generate using
     *
     * -> Generate a 256-bit (32-byte) key
     * $ openssl rand -base64 32
     *
     * -> Generate a 128-bit (16-byte) IV
     * $ openssl rand -base64 16
     */
    @Bean
    public String secretKey() {
        return dotenv.get("AES_SECRET_KEY");
    }

    @Bean
    public String iv() {
        return dotenv.get("AES_IV");
    }
}
