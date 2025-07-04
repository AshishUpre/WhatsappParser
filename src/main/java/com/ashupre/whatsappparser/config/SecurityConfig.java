package com.ashupre.whatsappparser.config;

import com.ashupre.whatsappparser.service.CustomOAuth2UserService;
import jakarta.servlet.http.HttpSessionEvent;
import jakarta.servlet.http.HttpSessionListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * added depends on cause was getting mongo error due to conn string being wrong
 * meaning when creating mongo related beans, env wasnt loaded and mongo related beans created after this
 * class cause this class needs custom oauth2 user service, that needs userRepo which needs mongo
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@DependsOn("envConfig")
@Slf4j
public class SecurityConfig {

    private final CustomOAuth2UserService customOAuth2UserService;

    private final String ec2Url;

    @Bean
    public CorsConfigurationSource corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();

        // left -> local testing 5173 - vite default | right -> aws s3 bucket hosted website
        config.setAllowedOriginPatterns(List.of("*"));
        // config.setAllowedOrigins(List.of("http://localhost:5173", "http://49.37.131.70"));
        config.addAllowedMethod("*");
        config.addAllowedHeader("*");
        config.setAllowCredentials(true);
        // config.addExposedHeader("Set-Cookie");
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
                //.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(registry -> registry
                        .requestMatchers("/login", "/api/user/logout", "/api/files/dummy"
                                , "/actuator/**", "/curr/thread").permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth2Login -> {
                    // oauth2Login.loginPage("/api/auth/login");
                    // register this service to get user details and put it in db after getting user info from provider
                    oauth2Login.userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService));
                    oauth2Login.failureHandler((request, response, exception) -> {
                        System.out.println("failure handler reached ====================================== ");
                        System.out.println("exception: " + exception);
                    });
                    // after auth it goes to /callback which will do some stuff and send to /dashboard
                    oauth2Login.successHandler((request, response, authentication) -> {
                        log.info("success handler reached SUCCESS REQUEST ========================================= ");

                        // explicit redirect to /callback as present on same ec2
                        String redirectUrl = ec2Url + "/callback";
                        response.sendRedirect(redirectUrl);
                    });
                })
                .logout(httpSecurityLogoutConfigurer -> {
                    httpSecurityLogoutConfigurer
                            .clearAuthentication(true)
                            .invalidateHttpSession(true)
                            // this will delete cookie JSESSIONID on logout
                            .deleteCookies("JSESSIONID")
                            .logoutUrl("/api/user/logout");
                });
        // .formLogin(Customizer.withDefaults()); // gives default form for login
        // FOR JWT
        // .addFilterBefore(new JwtAuthenticationFilter(aesUtil, jwtService ,userDetailsService), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public HttpSessionListener httpSessionListener() {
        return new HttpSessionListener() {
            @Override
            public void sessionCreated(HttpSessionEvent se) {
                log.info("\n ======================== Session Created: {}", se.getSession().getId());
            }

            @Override
            public void sessionDestroyed(HttpSessionEvent se) {
                log.info("Session Destroyed: {}", se.getSession().getId());
            }
        };
    }
}
