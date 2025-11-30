package com.springboot.FlomadAIplanner.config;

import com.springboot.FlomadAIplanner.filter.RateLimitingFilter;
import com.springboot.FlomadAIplanner.service.ratelimit.RateLimitService;
import com.springboot.FlomadAIplanner.util.JwtUtil;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.web.servlet.FilterRegistrationBean;

import java.time.Duration;

@Configuration
public class RateLimitConfig {

    @Bean
    public Bucket googlePlacesRateLimiter() {
        // Google Places API has a limit of 100 requests per second per IP
        // We'll set our limit lower to be safe
        Bandwidth limit = Bandwidth.classic(50, Refill.greedy(50, Duration.ofSeconds(1)));
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }
    
    /**
     * Create the rate limiting filter bean
     */
    @Bean
    public RateLimitingFilter rateLimitingFilter(RateLimitService rateLimitService, JwtUtil jwtUtil) {
        return new RateLimitingFilter(rateLimitService, jwtUtil);
    }
    
    /**
     * Register the rate limiting filter
     */
    @Bean
    public FilterRegistrationBean<RateLimitingFilter> rateLimitingFilterRegistration(RateLimitingFilter filter) {
        FilterRegistrationBean<RateLimitingFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(filter);
        registration.addUrlPatterns("/api/*"); // Apply to all API endpoints
        registration.setOrder(1); // Execute early in filter chain
        registration.setName("rateLimitingFilter");
        return registration;
    }
}
