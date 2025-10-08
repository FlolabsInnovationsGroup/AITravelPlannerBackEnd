package com.springboot.FlomadAIplanner.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
}
