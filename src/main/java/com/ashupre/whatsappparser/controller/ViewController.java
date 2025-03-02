package com.ashupre.whatsappparser.controller;

import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * @RestController returns plain text or JSON.
 * @Controller returns HTML templates via Thymeleaf.
 */
@Controller
public class ViewController {

    @GetMapping("/profile")
    public String profile(OAuth2AuthenticationToken token, Model model) {
        System.out.println("User photo URL: " + token.getPrincipal().getAttribute("picture"));
        model.addAttribute("name" , token.getPrincipal().getAttribute("name"));
        model.addAttribute("email", token.getPrincipal().getAttribute("email"));
        model.addAttribute("photo", token.getPrincipal().getAttribute("picture"));
        return "user-profile";
    }

    @GetMapping("/login")
    public String login() {
        return "custom-login";
    }
}
