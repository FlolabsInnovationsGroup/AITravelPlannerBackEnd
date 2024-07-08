package com.springboot.FlomadAIplanner.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {
    @SuppressWarnings("removal")
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf().disable() // Disable CSRF protection
            .authorizeHttpRequests(authz -> authz
                // Permit access without authentication to specific endpoints
                .requestMatchers("/api/v1/places/nearby").permitAll()
                .requestMatchers("/planTrip").permitAll() // Allow access to /planTrip without authentication
                .requestMatchers("/flight-offers").permitAll() // Allow anonymous access to /flight-offers
                .requestMatchers("/login").permitAll() // Allow access to /login without authentication
                .requestMatchers("/error").permitAll() // Allow access to /error without authentication
                .requestMatchers("/convert").permitAll()
                .anyRequest().authenticated() // All other requests need authentication
            )
            .formLogin(form -> form
                .loginPage("/login") // Custom login page
                .defaultSuccessUrl("/home") // Redirect to home after successful login
                .failureUrl("/login?error=true") // Redirect to login page with error parameter after failed login
                .permitAll() // Allow access to the login page without authentication
            )
            .logout(logout -> logout.permitAll()); // Allow access to the logout functionality without authentication

        return http.build();
    }
}
