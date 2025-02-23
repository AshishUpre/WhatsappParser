package com.ashupre.whatsappparser.util;

import com.ashupre.whatsappparser.security.AESUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseCookie;

import java.util.Arrays;
import java.util.Optional;

/**
 * provides methods for cookies related stuff
 */
public class CookieUtil {

    public static ResponseCookie createSecureHttpJwtCookieWithEncryptedValues(String name, String value, AESUtil aesUtil, long maxAge) {
        return ResponseCookie.from(name, aesUtil.encrypt(value))
                .httpOnly(true)
                .secure(false)
                .sameSite("Strict") // Set true in production (HTTPS required)
                .path("/")
                .maxAge(maxAge) // 1 hour
                .build();
    }

    /**
     * when given a request, returns the decrypted value of the cookie with the given name
     */
    public static String getDecryptedCookieValue(HttpServletRequest request, String cookieName, AESUtil aesUtil) throws RuntimeException{
        Cookie cookie = Arrays.stream(request.getCookies())
                .filter(c -> c.getName().equals(cookieName))
                .findFirst()
                .orElseThrow(() -> new RuntimeException(cookieName + " cookie not found"));

        return aesUtil.decrypt(cookie.getValue());
    }

    public static boolean checkCookiePresent(HttpServletRequest request, String cookieName) {
        Optional<Cookie> cookie = Arrays.stream(request.getCookies())
                .filter(c -> c.getName().equals(cookieName))
                .findFirst();

        return cookie.isPresent();
    }

}
