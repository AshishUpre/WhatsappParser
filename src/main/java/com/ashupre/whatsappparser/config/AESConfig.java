package com.ashupre.whatsappparser.config;

import com.ashupre.whatsappparser.security.AESUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AESConfig {

    @Bean
    public AESUtil aesUtil(String secretKey, String iv) {
        System.out.println("======================= reached here ============================================= ");
        System.out.println(secretKey);
        System.out.println(iv);
        return new AESUtil(secretKey, iv);
    }
}
