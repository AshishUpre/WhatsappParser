package com.ashupre.whatsappparser.util;

import com.ashupre.whatsappparser.security.AESUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Arrays;

/**
 * This class provides method to
 */
public class CookieUtil {

    /**
     *
     */
    public static Cookie createSecureHttpCookieWithEncryptedValues(String name, String value, AESUtil aesUtil) {
        Cookie cookie = new Cookie(name, aesUtil.encrypt(value));
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/"); // accessible across the whole app
        return cookie;
    }

    /**
     * when given a request, returns the decrypted value of the cookie with the given name
     */
    public static String getDecryptedCookieValue(HttpServletRequest request, String cookieName, AESUtil aesUtil) {
        Cookie cookie = Arrays.stream(request.getCookies())
                .filter(c -> c.getName().equals(cookieName))
                .findFirst()
                .orElseThrow(() -> new RuntimeException(cookieName + " cookie not found"));

        return aesUtil.decrypt(cookie.getValue());
    }
}
