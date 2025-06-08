package com.ashupre.whatsappparser.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Configuration
public class MiscConfig {

    @Bean
    public DateTimeFormatter inputFormatter() {
        return DateTimeFormatter.ofPattern("dd/MM/yy, [h][hh]:mm a");
    }

    @Bean
    public DateTimeFormatter outputFormatter() {
        return DateTimeFormatter.ofPattern("dd/MM/yy, h:mm a");
    }

    @Bean
    public ZoneId asiaKolkataZoneId() {
        return ZoneId.of("Asia/Kolkata");
    }
}
