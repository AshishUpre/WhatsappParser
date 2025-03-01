package com.ashupre.whatsappparser.config;

import com.ashupre.whatsappparser.service.CustomOAuth2UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

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
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE"));
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
                // .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(registry -> registry
                        .requestMatchers("/", "/login").permitAll()
                        .requestMatchers("/api/auth/login", "/api/auth/register").permitAll()
                        .requestMatchers("/api/users/logout").permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth2Login -> {
                    oauth2Login.loginPage("/api/auth/login");
                    // register this service to get user details and put it in db after getting user info from provider
                    System.out.println("reached here in security config");
                    oauth2Login.userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService));
                    // after auth it goes to /profile
                    oauth2Login.successHandler((request, response, authentication) -> response.sendRedirect("http://localhost:5173/callback"));
                });
                // give default form for login
                // .formLogin(Customizer.withDefaults());
                // .addFilterBefore(new JwtAuthenticationFilter(aesUtil, jwtService ,userDetailsService), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
