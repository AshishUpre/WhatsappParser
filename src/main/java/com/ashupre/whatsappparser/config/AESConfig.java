package com.ashupre.whatsappparser.config;

import com.ashupre.whatsappparser.security.AESUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AESConfig {

    @Bean
    public AESUtil aesUtil(String secretKey, String iv) {
        return new AESUtil(secretKey, iv);
    }
}
