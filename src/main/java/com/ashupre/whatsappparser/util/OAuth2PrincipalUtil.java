package com.ashupre.whatsappparser.util;

import lombok.NonNull;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;

import java.security.Principal;

public class OAuth2PrincipalUtil {

    public static String getAttributes(@NonNull Principal principal, @NonNull String attributeName) {
        if (principal instanceof OAuth2AuthenticationToken token) {
            return (String) token.getPrincipal().getAttributes().get(attributeName);
        }
        return null;
    }
}
