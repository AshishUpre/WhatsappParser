package com.ashupre.whatsappparser.service;

import com.ashupre.whatsappparser.model.User;
import com.ashupre.whatsappparser.repository.UserRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * CustomOAuth2UserService is a custom implementation of DefaultOAuth2UserService, responsible for handling
 * OAuth2 user authentication in a Spring Boot application. This service retrieves user details from an OAuth2
 * provider (such as Google or GitHub), processes the user information, and stores it in the database.
 * <p>
 * the
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    private final EmailService emailService;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        log.debug(" =================================== about to load user ===============================" +
                "============ ");

        Map<String, Object> attributes = new HashMap<>(oAuth2User.getAttributes());
        String provider = userRequest.getClientRegistration().getRegistrationId(); // "google" "github" etc
        String providerId = attributes.get(getProviderIdName(provider)).toString(); // Google uses "sub", GitHub uses "id"
        String email = attributes.get("email").toString();
        String name = attributes.get("name").toString();
        String profilePic = attributes.get("picture") != null ? attributes.get("picture").toString() : null;

        // save or update user DB - if we dont findByEmail save
        Optional<User> optionalUser = userRepository.findByEmail(email);
        User user = optionalUser.orElseGet(()
                -> createNewUser(email, name, profilePic, provider, providerId));
        log.debug("user got from db : {}", user);

        // save (mongodb save method will insert if new, or update if exists)
        User savedUser = userRepository.save(user);
        log.debug("Saved user: {}", savedUser);

        attributes.put("provider", provider); // injecting provider name into attributes to be used everywhere else
        return new DefaultOAuth2User(
                oAuth2User.getAuthorities(),
                attributes,
                "name"
        );
    }

    private User createNewUser/*AndSendWelcomeEmail*/(String email, String name, String profilePic,
                                                  String provider, String providerId) {
        System.out.println("New user detected, sending email through lambda, emailId = " + email);
//        emailService.sendWelcomeEmail(name, email);
        return User.builder()
                .oAuthProvider(provider)
                .oAuthProviderUserId(providerId)
                .email(email)
                .name(name)
                .profilePic(profilePic)
                .build();
    }

    /**
     * Returns what the providerId is called for different providers
     */
    public static String getProviderIdName(@NonNull String provider) {
        return switch (provider) {
            case "google" -> "sub";
            case "github" -> "id";
            default -> throw new RuntimeException("Unknown provider: " + provider);
        };
    }
}
