package com.ashupre.whatsappparser.filter;

import com.ashupre.whatsappparser.security.AESUtil;
import com.ashupre.whatsappparser.service.CustomUserDetailsService;
import com.ashupre.whatsappparser.service.JwtService;
import com.ashupre.whatsappparser.util.CookieUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.annotation.Nonnull;
import java.io.IOException;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final AESUtil aesUtil;
    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(@Nonnull HttpServletRequest request,
                                    @Nonnull HttpServletResponse response,
                                    @Nonnull FilterChain filterChain) throws ServletException, IOException {
        // this will be null as we are using cookies instead of auth header
        System.out.println("JWT Authorization Header: " + request.getHeader("Authorization"));

        String token;
        try {
            token = CookieUtil.getDecryptedCookieValue(request, "jwt", aesUtil);
            System.out.println("token : " + token);
        } catch (Exception e) {
            System.out.println("exception : " + e.getMessage());
            token = null;
        }

        if (token != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            String email = jwtService.extractEmail(token);

            if (email != null) {
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(email, null,
                                userDetailsService.loadUserByUsername(email).getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }
        filterChain.doFilter(request, response);
    }
}
