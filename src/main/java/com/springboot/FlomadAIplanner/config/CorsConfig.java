package com.springboot.FlomadAIplanner.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;
import java.util.Collections;

@Configuration
public class CorsConfig {

    @Value("${application.cors.allowed.origins}")
    private String[] allowedOrigins;

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        
        // Configure CORS settings
        config.setAllowCredentials(true);
        
        // Use configured allowed origins instead of wildcard
        if (allowedOrigins != null && allowedOrigins.length > 0) {
            config.setAllowedOrigins(Arrays.asList(allowedOrigins));
            System.out.println("🔧 CORS configured with origins: " + Arrays.toString(allowedOrigins));
        } else {
            // Fallback to wildcard pattern if no origins configured
            config.setAllowedOriginPatterns(Collections.singletonList("*"));
            System.out.println("⚠️ CORS fallback to wildcard pattern");
        }
        
        config.setAllowedHeaders(Arrays.asList(
            "Origin", 
            "Content-Type", 
            "Accept", 
            "Authorization",
            "Access-Control-Allow-Origin",
            "Access-Control-Request-Method",
            "Access-Control-Request-Headers",
            "Access-Control-Allow-Credentials",
            "X-Requested-With",
            "Sec-WebSocket-Key",
            "Sec-WebSocket-Version",
            "Sec-WebSocket-Extensions",
            "Sec-WebSocket-Protocol",
            "Sec-WebSocket-Accept"
        ));
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        config.setMaxAge(3600L);
        
        // Apply CORS configuration to all paths
        source.registerCorsConfiguration("/**", config);
        source.registerCorsConfiguration("/ws/**", config);
        source.registerCorsConfiguration("/ws/info", config);
        source.registerCorsConfiguration("/topic/**", config);
        
        return new CorsFilter(source);
    }

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                // Use configured origins or fallback to wildcard
                String[] origins = (allowedOrigins != null && allowedOrigins.length > 0) 
                    ? allowedOrigins 
                    : new String[]{"*"};
                
                String[] apiOrigins = Arrays.copyOf(origins, origins.length + 1);
                apiOrigins[origins.length] = "https://api.flomadtravel.com";

                // Configure for API endpoints
                registry.addMapping("/api/**")
                    .allowedOrigins(apiOrigins)
                    .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
                    .allowedHeaders("*")
                    .allowCredentials(allowedOrigins != null && allowedOrigins.length > 0)
                    .maxAge(3600);
                
                // Configure for all user endpoints (including /users/*)
                registry.addMapping("/users/**")
                    .allowedOrigins(origins)
                    .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
                    .allowedHeaders("*")
                    .allowCredentials(allowedOrigins != null && allowedOrigins.length > 0)
                    .maxAge(3600);
                
                // Configure for WebSocket endpoints
                registry.addMapping("/ws/**")
                    .allowedOrigins(origins)
                    .allowedMethods("GET", "POST", "OPTIONS")
                    .allowedHeaders("*")
                    .allowCredentials(allowedOrigins != null && allowedOrigins.length > 0)
                    .maxAge(3600);
                
                // Configure for WebSocket info endpoint
                registry.addMapping("/ws/info")
                    .allowedOrigins(origins)
                    .allowedMethods("GET", "OPTIONS")
                    .allowedHeaders("*")
                    .allowCredentials(allowedOrigins != null && allowedOrigins.length > 0)
                    .maxAge(3600);
            }
        };
    }
}
