package com.springboot.FlomadAIplanner.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(authorize -> authorize
                .requestMatchers("/sessions/**", "/login", "/error", "/oauth2/**").permitAll()
                .anyRequest().authenticated()
            )
            .oauth2Login(oauth2 -> oauth2
                .successHandler((request, response, authentication) -> {
                    // Determine redirect URL based on environment
                    String redirectUrl = request.getRequestURL().toString().contains("api.flomadtravel.com") 
                        ? "https://www.flomadtravel.com/chat" 
                        : "http://localhost:3000/chat";
                    
                    response.setHeader("Access-Control-Allow-Origin", 
                        redirectUrl.startsWith("https://www.flomadtravel.com") 
                            ? "https://www.flomadtravel.com" 
                            : "http://localhost:3000");
                    response.setHeader("Access-Control-Allow-Credentials", "true");
                    response.sendRedirect(redirectUrl);
                })
            );
        
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // Allow both local development and production domains
        configuration.setAllowedOrigins(Arrays.asList(
            "http://localhost:3000",
            "https://www.flomadtravel.com",
            "https://flomadtravel.com"
        ));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
