package com.ashupre.whatsappparser.config;

import com.ashupre.whatsappparser.service.CustomOAuth2UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.DefaultAuthenticationEventPublisher;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.CookieClearingLogoutHandler;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.io.IOException;
import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomOAuth2UserService customOAuth2UserService;

    @Bean
    public CorsConfigurationSource corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();

        // 5173 - vite default
        config.setAllowedOrigins(List.of("http://localhost:3000", "http://localhost:5173"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsFilter()))
                .csrf(csrf -> csrf.disable())
                // for jwt we used stateless, if we use stateless even for oauth2, every time even after user logging in
                // they will have to login again when going to different route => no persistance of session
                // => dont make the session stateless so that user can stay logged in
//                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(registry -> registry
                        .requestMatchers("/login", "/api/user/logout").permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth2Login -> {
                    // oauth2Login.loginPage("/api/auth/login");
                    // register this service to get user details and put it in db after getting user info from provider
                    oauth2Login.userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService));
                    // after auth it goes to /profile
                    oauth2Login.successHandler((request, response, authentication) -> response.sendRedirect("http://localhost:5173/callback"));
                })
                .logout(httpSecurityLogoutConfigurer -> {
                    httpSecurityLogoutConfigurer
                            .clearAuthentication(true)
                            .invalidateHttpSession(true)
                            .deleteCookies("JSESSIONID")
                            // .logoutUrl("/api/user/logout")
                            // this not working either - no session, no cookies found, authentication is null
                            .addLogoutHandler((request, response, authentication) -> {
                                SecurityContextHolder.clearContext();
                                HttpSession session = request.getSession(false);
                                if (session != null) {
                                    System.out.println("session not invalidated yet, hence invalidating");
                                    session.invalidate();
                                }

                                // Check if cookies are present before processing them
                                if (request.getCookies() != null) {
                                    for (Cookie cookie : request.getCookies()) {
                                        System.out.println(cookie.getName());
                                        cookie.setMaxAge(0);
                                        cookie.setPath("/");
                                        response.addCookie(cookie);
                                    }
                                } else {
                                    System.out.println("No cookies found.");
                                }
                                System.out.println("reached custom logout handler ============================== 111111");
                            })
                            .logoutSuccessHandler((request, response, authentication) -> {
                                //  NOT WORKING
                                // Clear the JSESSIONID cookie by setting it with an expiration date in the past
                                // Cookie cookie = new Cookie("JSESSIONID", null);
                                // cookie.setPath("/"); // Ensure the path matches the one used for the cookie
                                // cookie.setMaxAge(0); // Set the max age to 0 to delete the cookie
                                // response.addCookie(cookie); // Add the cookie to the response

                                // Send an OK response
                                System.out.println("reached custom logout handler ============================== ");
                            });
                });
                // give default form for login
                // .formLogin(Customizer.withDefaults());
                // FOR JWT
                // .addFilterBefore(new JwtAuthenticationFilter(aesUtil, jwtService ,userDetailsService), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

}
