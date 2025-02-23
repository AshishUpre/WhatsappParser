package com.ashupre.whatsappparser.service;

import com.ashupre.whatsappparser.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import com.ashupre.whatsappparser.model.User;

/**
 * we need a custom user details service to get the user details from the database because we are using email
 * as the username instead of 'username' which is the default. If we had used username instead then the default
 * JdbcUserDetailsManager would have been enough. As we used @Service, no need to have an explicit bean in
 * security config
 *
 * spring documentation :

     UserDetailsService is used by DaoAuthenticationProvider for retrieving a username, a password, and other attributes
     for authenticating with a username and password. Spring Security provides in-memory, JDBC, and caching
     implementations of UserDetailsService.

     You can define custom authentication by exposing a custom UserDetailsService as a bean.
     For example, the following listing customizes authentication, assuming that CustomUserDetailsService
     implements UserDetailsService

         @Bean
         CustomUserDetailsService customUserDetailsService() {
            return new CustomUserDetailsService();
         }
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new UsernameNotFoundException("User not found");
        }
        UserDetails userDetails = org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password(user.getPassword())
                .roles("USER").build();

        System.out.println("user details in CustomUserDetailsService: " + userDetails);
        return userDetails;
    }
}
