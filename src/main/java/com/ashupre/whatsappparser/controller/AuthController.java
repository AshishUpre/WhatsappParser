package com.ashupre.whatsappparser.controller;

import com.ashupre.whatsappparser.dto.UserDTO;
import com.ashupre.whatsappparser.model.User;
import com.ashupre.whatsappparser.repository.UserRepository;
import com.ashupre.whatsappparser.security.AESUtil;
import com.ashupre.whatsappparser.service.AuthenticationService;
import com.ashupre.whatsappparser.util.CookieUtil;
import com.nimbusds.jose.JOSEException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final UserRepository userRepository;

    private final AuthenticationService authService;
    private final AESUtil aesUtil;

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody UserDTO userDTO) {
        try {
            String jwt = authService.authenticate(userDTO.getEmail(), userDTO.getPassword());
            ResponseCookie jwtCookie = CookieUtil.createSecureHttpJwtCookieWithEncryptedValues("jwt", jwt,
                    aesUtil, 3600);

            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
                    .body("Login successful");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid email or password");
        }
    }


    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@RequestBody UserDTO userDTO) throws JOSEException {
        if (userRepository.existsByEmail(userDTO.getEmail())) {
            return ResponseEntity.badRequest().body("Email already exists");
        }
        User user = new User();
        user.setUsername(userDTO.getUsername());
        user.setEmail(userDTO.getEmail());
        user.setPassword(userDTO.getPassword());

        User savedUser = userRepository.save(user);

        String jwt = authService.authenticate(savedUser.getEmail(), savedUser.getPassword());
        ResponseCookie jwtCookie = CookieUtil.createSecureHttpJwtCookieWithEncryptedValues("jwt", jwt, aesUtil, 3600);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
                .body("User registered successfully");
    }
}
