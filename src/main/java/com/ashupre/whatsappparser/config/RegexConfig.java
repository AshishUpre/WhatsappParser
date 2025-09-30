package com.ashupre.whatsappparser.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.regex.Pattern;

@Configuration
public class RegexConfig {

    @Bean
    public Pattern messagePattern() {
        return Pattern.compile(
                // * \h? instead of a space before [ap]m
                // * This allows for either a normal space or a non-breaking space (newer exports have small
                // * gap between that is not a space -> non-breaking space).
                // * similarly for beside -
                "^(\\d{2}/\\d{2}/\\d{2}, \\d{1,2}:\\d{2}[\\h]?[ap]m)[\\h]-[\\h](.*?):\\s(.*)$"
        );
    }
}
