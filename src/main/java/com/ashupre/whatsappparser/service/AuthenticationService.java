package com.ashupre.whatsappparser.service;

import com.nimbusds.jose.JOSEException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.CredentialsContainer;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final JwtService jwtService;

    private final AuthenticationManager authenticationManager;

    /**
     * User user = userService.getUserByEmail(email);
     *         if (user == null || !user.getPassword().equals(password)) {
     *             throw new RuntimeException("Invalid email or password");
     *         }
     * return jwtService.createJwt(email);
     *
     * * this is implementing it by ourself, but we dont have to do this as AuthenticationManager has its
     * * own implementation to find user and authenticate. We use the authenticationManager bean that we created in
     * * security config and use it to authenticate the user. Afterwards we clear the container to prevent
     * * leakage
     */
    public String authenticate(String email, String password) throws JOSEException {
        Authentication authenticationResult = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password)
        );
        System.out.println("auth result : " + authenticationResult);

        SecurityContextHolder.getContext().setAuthentication(authenticationResult);

        // we clear it as it contains credentials
        if (authenticationResult instanceof CredentialsContainer container) {
            container.eraseCredentials();
        }
        return jwtService.createJwt(email);
    }
}
