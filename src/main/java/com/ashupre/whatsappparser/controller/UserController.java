package com.ashupre.whatsappparser.controller;

import com.ashupre.whatsappparser.model.LoginRequest;
import com.ashupre.whatsappparser.model.User;
import com.ashupre.whatsappparser.model.UserDTO;
import com.ashupre.whatsappparser.security.AESUtil;
import com.ashupre.whatsappparser.service.UserService;
import com.ashupre.whatsappparser.util.CookieUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    private final AESUtil aesUtil;

    // Create a new user (Registration)
    @PostMapping("/register")
    public ResponseEntity<User> registerUser(@RequestBody UserDTO userDTO, HttpServletResponse response) {
        User savedUser = userService.createUser(userDTO.getUsername(),
                userDTO.getEmail(),
                userDTO.getPassword(),
                userDTO.getFile());

        Cookie emailCookie = CookieUtil.createSecureHttpCookieWithEncryptedValues("email", savedUser.getEmail(),
                aesUtil);
        Cookie idCookie = CookieUtil.createSecureHttpCookieWithEncryptedValues("userId", savedUser.getId(),
                aesUtil);

        response.addCookie(emailCookie);
        response.addCookie(idCookie);
        return ResponseEntity.ok(savedUser);
    }

    // Login user
    @PostMapping("/login")
    public ResponseEntity<User> loginUser(@RequestBody LoginRequest loginRequest, HttpServletRequest request,
            HttpServletResponse response) {
        System.out.println("got login request body: " + loginRequest);
        String email = CookieUtil.getDecryptedCookieValue(request, "email", aesUtil); // Decrypt and get the actual values
        String userId = CookieUtil.getDecryptedCookieValue(request, "userId", aesUtil);
        System.out.println("got email: " + email);
        System.out.println("got userId: " + userId);
        User savedUser;
        boolean authenticated;

        // this condition means the cookie was cleared previously on prev logout
        if (email.equals("") && userId.equals("")) {
            savedUser = userService.getUserByEmail(loginRequest.getEmail());
            authenticated = userService.authenticateUser(savedUser.getId(), loginRequest.getPassword());
        } else {
            savedUser = userService.getUserByEmail(email);
            authenticated = userService.authenticateUser(savedUser.getId(), savedUser.getPassword());
        }

        Cookie emailCookie = CookieUtil.createSecureHttpCookieWithEncryptedValues("email", savedUser.getEmail(),
                aesUtil);
        Cookie idCookie = CookieUtil.createSecureHttpCookieWithEncryptedValues("userId", savedUser.getId(),
                aesUtil);

        if (authenticated) {
            response.addCookie(emailCookie);
            response.addCookie(idCookie);
            return ResponseEntity.ok(savedUser);
        } else {
            return ResponseEntity.status(401).header("Error",
                    "Invalid credentials").body(null);
        }
    }

    // logout user - need this to clear the cookie in the browser
    @PostMapping("/logout")
    public ResponseEntity<String> logoutUser(HttpServletRequest request, HttpServletResponse response) {
//        String email = CookieUtil.getDecryptedCookieValue(request, "email", aesUtil); // Decrypt and get the actual values
//        String userId = CookieUtil.getDecryptedCookieValue(request, "userId", aesUtil);
//        System.out.println("got email: " + email);
//        System.out.println("got userId: " + userId);
        Cookie userIdCookie = CookieUtil.createSecureHttpCookieWithEncryptedValues("userId", "", aesUtil);
        Cookie emailCookie = CookieUtil.createSecureHttpCookieWithEncryptedValues("email", "", aesUtil);
        response.addCookie(userIdCookie);
        response.addCookie(emailCookie);
        return ResponseEntity.ok("Logged out successfully");
    }

    // Update profile picture
    @PutMapping("/profile-pic")
    public ResponseEntity<String> updateProfilePic(HttpServletRequest request, @RequestParam("file") MultipartFile file) {
        String userId = CookieUtil.getDecryptedCookieValue(request, "userId", aesUtil);
        userService.updateProfilePic(userId, file);
        return ResponseEntity.ok("Profile picture updated successfully.");
    }

    // Get all users
    @GetMapping("/all")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/files")
    public ResponseEntity<List<String>> getAllFilesOfUser(HttpServletRequest request) {
        String userId = CookieUtil.getDecryptedCookieValue(request, "userId", aesUtil);
        return ResponseEntity.ok(userService.getAllFilesOfUser(userId));
    }

    @DeleteMapping("/delete")
    public ResponseEntity<String> deleteUser(HttpServletRequest request) {
        String userId = CookieUtil.getDecryptedCookieValue(request, "userId", aesUtil);
        String email = CookieUtil.getDecryptedCookieValue(request, "email", aesUtil);
        userService.deleteUserById(userId);
        return ResponseEntity.ok("User deleted successfully.");
    }

}
