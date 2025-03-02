package com.ashupre.whatsappparser.service;

import com.ashupre.whatsappparser.model.User;
import com.ashupre.whatsappparser.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import java.util.Map;
import com.fasterxml.jackson.databind.ObjectMapper;
/**
 * CustomOAuth2UserService is a custom implementation of DefaultOAuth2UserService, responsible for handling
 * OAuth2 user authentication in a Spring Boot application. This service retrieves user details from an OAuth2
 * provider (such as Google or GitHub), processes the user information, and stores it in the database.
 *
 * the
 */
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        System.out.println(" =================================== reached here ========================================== ");

        Map<String, Object> attributes = oAuth2User.getAttributes();
        String provider = userRequest.getClientRegistration().getRegistrationId(); // "google", "github"
        String providerId = attributes.get("sub").toString(); // Google uses "sub", GitHub uses "id"
        String email = attributes.get("email").toString();
        String name = attributes.get("name").toString();
        String profilePic = attributes.get("picture") != null ? attributes.get("picture").toString() : null;

        // save or update user DB
        User user = userRepository.findByEmail(email)
                .orElse(User.builder()
                        .provider(provider)
                        .providerId(providerId)
                        .email(email)
                        .name(name)
                        .profilePic(profilePic)
                        .build()
                );

        System.out.println("user " + user);
        // save (mongodb save method will insert if new, or update if exists)
        userRepository.save(user);
        return new DefaultOAuth2User(
                oAuth2User.getAuthorities(),
                attributes,
                "name"
        );
    }
}
