package com.springboot.FlomadAIplanner.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig {

    @Value("${application.front-end.url}")
    private String frontEndUrl;


    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                System.out.println("Adding CORS Mappings");
                registry.addMapping("/**").allowedOrigins(
                                "http://localhost:4200",
                                frontEndUrl
                        ).allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .allowedHeaders("*")
                        .allowCredentials(true);
            }
        };
    }
}

// @Configuration
// public class WebConfig {

//     @Bean
//     public CorsConfigurationSource corsConfigurationSource() {
//         CorsConfiguration config = new CorsConfiguration();
//         config.setAllowCredentials(true);
//         config.addAllowedOrigin("*");
//         config.addAllowedHeader("*");
//         config.addAllowedMethod("*");

//         UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
//         source.registerCorsConfiguration("/**", config);
//         return source;
//     }
// }