package com.ashupre.whatsappparser.controller;

import com.ashupre.whatsappparser.dto.UserDTO;
import com.ashupre.whatsappparser.exceptions.UserNotFoundException;
import com.ashupre.whatsappparser.service.FileDataService;
import com.ashupre.whatsappparser.service.UserService;
import com.ashupre.whatsappparser.util.OAuth2PrincipalUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.util.Pair;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Arrays;
import java.util.List;

import static com.ashupre.whatsappparser.service.CustomOAuth2UserService.getProviderIdName;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;

    private final FileDataService fileDataService;

    /**
     * the principal:user is the user that is logged in, its autowired
     * similarly OAuth2AuthenticationToken is also present. from that token we can get the principal too
     */
    @GetMapping("/me")
    public ResponseEntity<UserDTO> getUser(Principal user, Authentication authentication, HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            log.info("session is null in user controller");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }
        if (authentication == null || !authentication.isAuthenticated()) {
            log.info("authentication / not authenticated is null in user controller");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }
        System.out.println("User : " + user);
        return ResponseEntity.ok(new UserDTO(user));
    }

    // not needed, just for testing
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
        log.info("reached user controller, get all files of user");
        log.info("request: " + Arrays.toString(Arrays.stream(request.getCookies()).toArray()));

        // get providerName through that get providerIdName using which get providerId and use it to find User
        String oauthProvider = OAuth2PrincipalUtil.getAttributes(user, "provider");
        String providerId = OAuth2PrincipalUtil.getAttributes(user, getProviderIdName(oauthProvider));
        List<Pair<String, String>> files = fileDataService.getAllFilesOfUser(
                    userService.findByOAuthProviderUserId(providerId).orElseThrow(() ->
                            new UserNotFoundException("User not found")
                    ).getId()
        );
        return ResponseEntity.ok(files);
    }

}
