package com.ashupre.whatsappparser.controller;

import com.ashupre.whatsappparser.model.User;
import com.ashupre.whatsappparser.security.AESUtil;
import com.ashupre.whatsappparser.service.FileDataService;
import com.ashupre.whatsappparser.service.UserService;
import com.ashupre.whatsappparser.util.CookieUtil;
import com.ashupre.whatsappparser.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.util.Pair;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    private final FileDataService fileDataService;

    private final AESUtil aesUtil;

    /**
     * an authenticated user can get their details through this endpoint, can be used for user profile page
     * after authentication. Gets the user from the authentication principal (we can do this manually by using
     * securitycontextholder but this is cleaner)
     */
    @GetMapping("/me")
    public ResponseEntity<User> getUser(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(user);
    }

    // logout user - need this to clear the cookie in the browser
    @PostMapping("/logout")
    public ResponseEntity<String> logoutUser(Authentication authentication) {
        if (authentication != null) {
            SecurityContextHolder.getContext().setAuthentication(null);
        }
        ResponseCookie expiredCookie = CookieUtil.createSecureHttpJwtCookieWithEncryptedValues("jwt",
                aesUtil.encrypt(""), aesUtil, 0);

        // after logging out, clear the cookie to prevent session hijack
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, expiredCookie.toString())
                .body("Logged out successfully.");
    }

    // Update profile picture
    @PutMapping("/profile-pic")
    public ResponseEntity<String> updateProfilePic(HttpServletRequest request, @RequestParam("file") MultipartFile file) {
        String userId = CookieUtil.getDecryptedCookieValue(request, "userId", aesUtil);
        userService.updateProfilePic(userId, file);
        return ResponseEntity.ok()
                .body("Profile picture updated successfully.");
    }

    // Get all users
    @GetMapping("/all")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/files")
    public ResponseEntity<List<Pair<String, String>>> getAllFilesOfUser(HttpServletRequest request) {
        String jwt = CookieUtil.getDecryptedCookieValue(request, "jwt", aesUtil);
        String email = JwtUtil.extractEmail(jwt);
        String userId = userService.getUserByEmail(email).getId();

        return ResponseEntity.ok(fileDataService.getAllFilesOfUser(userId));
    }

    @DeleteMapping("/delete")
    public ResponseEntity<String> deleteUser(HttpServletRequest request, Authentication authentication) {
        String jwt = CookieUtil.getDecryptedCookieValue(request, "jwt", aesUtil);
        String email = JwtUtil.extractEmail(jwt);
        userService.deleteUserByEmail(email);

        System.out.println("authentication : " + authentication);
        if (authentication != null) {
            SecurityContextHolder.getContext().setAuthentication(null);
        }

        // we make an expired expiredCookie and send it to clear out the expiredCookie
        ResponseCookie expiredCookie = CookieUtil.createSecureHttpJwtCookieWithEncryptedValues("jwt",
                aesUtil.encrypt(""), aesUtil, 0);

        // after deletion clear out the expiredCookie of the user so that they cannot log in
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, expiredCookie.toString())
                .body("User deleted successfully.");
    }

}
