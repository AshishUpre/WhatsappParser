package com.ashupre.whatsappparser.config;

import io.github.cdimascio.dotenv.Dotenv;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EnvConfig {

    private final Dotenv dotenv = Dotenv.load();

    /**
     * manually setting oauth credentials
     * # get from console.cloud.google.com -> create new project / existing project -> left panel -> APIs & Services
     * # -> Credentials -> + create credentials (at top)
     * # first you need to configure consent screen for that
     * # whether using for internal or external -> if chosen internal only Google Workspace accounts can log in (companies buying)
     * # external -> users can use their gmail account
     */
    @PostConstruct
    public void loadOauthCredentials() {
        // these lines must be present in application.properties
        // spring.security.oauth2.client.registration.google.client-id=${GOOGLE_CLIENT_ID}
        // spring.security.oauth2.client.registration.google.client-secret=${GOOGLE_CLIENT_SECRET}
        System.setProperty("GOOGLE_CLIENT_ID", dotenv.get("GOOGLE_CLIENT_ID"));
        System.setProperty("GOOGLE_CLIENT_SECRET", dotenv.get("GOOGLE_CLIENT_SECRET"));
    }

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

    @Bean
    public String jwtSecret() {
        return dotenv.get("JWT_SECRET");
    }


}
