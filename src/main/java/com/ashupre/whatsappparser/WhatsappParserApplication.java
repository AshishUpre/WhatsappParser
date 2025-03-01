package com.ashupre.whatsappparser;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

@SpringBootApplication
@EnableWebSecurity
public class WhatsappParserApplication {

    public static void main(String[] args) {
        SpringApplication.run(WhatsappParserApplication.class, args);
    }

}
