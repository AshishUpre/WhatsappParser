package com.ashupre.whatsappparser.controller;

import com.ashupre.whatsappparser.dto.UserDTO;
import com.ashupre.whatsappparser.exceptions.UserNotFoundException;
import com.ashupre.whatsappparser.repository.UserRepository;
import com.ashupre.whatsappparser.service.FileDataService;
import com.ashupre.whatsappparser.service.UserService;
import com.ashupre.whatsappparser.util.OAuth2PrincipalUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.util.Pair;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    private final FileDataService fileDataService;

    private final UserRepository userRepository;

    /**
     * the principal:user is the user that is logged in, its autowired
     * similarly OAuth2AuthenticationToken is also present. from that token we can get the principal too
     */
    @GetMapping("/me")
    public ResponseEntity<UserDTO> getUser(Principal user) {
        System.out.println("principal : " + user);
        return ResponseEntity.ok(new UserDTO(user));
    }

    // logout user - need this to clear the cookie in the browser
    @PostMapping("/logout")
    public ResponseEntity<String> logoutUser(Principal user, HttpServletRequest request, HttpServletResponse response) {
        // Invalidate session
        System.out.println(" ============ reached logout ================= ");
        request.getSession().invalidate();
        response.setHeader(HttpHeaders.SET_COOKIE, "JSESSIONID=; Path=/; HttpOnly; Max-Age=0; Expires=Thu, 01 Jan 1970 00:00:00 GMT");

        if (user == null) {
            return ResponseEntity.ok()
                    // no use
                    // .header(HttpHeaders.SET_COOKIE, "JSESSIONID=; Max-Age=0; expires=Thu, 01 Jan 1970 00:00:00 GMT; path=/")
                    .body("Not logged in.");
        }
        return ResponseEntity.ok()
                .body("Logged out successfully.");
    }

    @GetMapping("/files")
    public ResponseEntity<List<Pair<String, String>>> getAllFilesOfUser(HttpServletRequest request, Principal user) {
        System.out.println("reached user controller, get all files of user");
        System.out.println("request: " + Arrays.toString(Arrays.stream(request.getCookies()).toArray()));
        System.out.println("user: " + user);

        // note: sub only applicable to google
        String providerId = OAuth2PrincipalUtil.getAttributes(user,"sub");
        List<Pair<String, String>> files = fileDataService.getAllFilesOfUser(
                    userRepository.findByProviderId(providerId).orElseThrow(() ->
                            new UserNotFoundException("User not found")
                    ).getId()
        );
        return ResponseEntity.ok(files);
    }

//    @DeleteMapping("/delete")
//    public ResponseEntity<String> deleteUser(HttpServletRequest request, Authentication authentication) {
//        String jwt = CookieUtil.getDecryptedCookieValue(request, "jwt", aesUtil);
//        String email = JwtUtil.extractEmail(jwt);
//        userService.deleteUserByEmail(email);
//
//        System.out.println("authentication : " + authentication);
//        if (authentication != null) {
//            SecurityContextHolder.getContext().setAuthentication(null);
//        }
//
//        // we make an expired expiredCookie and send it to clear out the expiredCookie
//        ResponseCookie expiredCookie = CookieUtil.createSecureHttpJwtCookieWithEncryptedValues("jwt",
//                aesUtil.encrypt(""), aesUtil, 0);
//
//        // after deletion clear out the expiredCookie of the user so that they cannot log in
//        return ResponseEntity.ok()
//                .header(HttpHeaders.SET_COOKIE, expiredCookie.toString())
//                .body("User deleted successfully.");
//    }

}
