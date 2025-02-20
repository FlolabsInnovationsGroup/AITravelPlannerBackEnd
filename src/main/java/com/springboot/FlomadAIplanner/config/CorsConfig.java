package com.springboot.FlomadAIplanner.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig {

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**") // ✅ Allow API routes
                        .allowedOrigins("http://localhost:3000") // ✅ Allow frontend
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // ✅ Allow OPTIONS for preflight
                        .allowCredentials(true) // ✅ Allow cookies & auth headers
                        .allowedHeaders("Authorization", "Content-Type"); // ✅ Ensure auth headers allowed
            }
        };
    }
}
