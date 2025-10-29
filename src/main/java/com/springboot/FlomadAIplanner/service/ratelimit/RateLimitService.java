package com.springboot.FlomadAIplanner.service.ratelimit;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Rate limiting service using Bucket4j for token bucket algorithm.
 * Provides different rate limits for different user types and API endpoints.
 */
@Service
public class RateLimitService {
    
    private static final Logger logger = LoggerFactory.getLogger(RateLimitService.class);
    
    // Cache for user-specific buckets
    private final ConcurrentHashMap<String, Bucket> userBuckets = new ConcurrentHashMap<>();
    
    /**
     * Rate limit types for different endpoints
     */
    public enum RateLimitType {
        CHAT_API,           // Chat endpoints
        GENERAL_API,        // General API endpoints  
        EXPENSIVE_API,      // AI-heavy operations
        GUEST_USER,         // Guest user operations
        AUTH_USER           // Authenticated user operations
    }
    
    /**
     * Check if request is allowed for the given user and rate limit type
     */
    public boolean isAllowed(String userId, RateLimitType rateLimitType) {
        String bucketKey = generateBucketKey(userId, rateLimitType);
        Bucket bucket = userBuckets.computeIfAbsent(bucketKey, 
            key -> createBucketForType(rateLimitType));
            
        boolean allowed = bucket.tryConsume(1);
        
        if (!allowed) {
            logger.warn("Rate limit exceeded for user: {} with type: {}", userId, rateLimitType);
        } else {
            logger.debug("Request allowed for user: {} with type: {}, remaining tokens: {}", 
                userId, rateLimitType, bucket.getAvailableTokens());
        }
        
        return allowed;
    }
    
    /**
     * Get remaining tokens for user and rate limit type
     */
    public long getRemainingTokens(String userId, RateLimitType rateLimitType) {
        String bucketKey = generateBucketKey(userId, rateLimitType);
        Bucket bucket = userBuckets.get(bucketKey);
        return bucket != null ? bucket.getAvailableTokens() : 0;
    }
    
    /**
     * Generate unique bucket key for user and rate limit type
     */
    private String generateBucketKey(String userId, RateLimitType rateLimitType) {
        return userId + ":" + rateLimitType.name();
    }
    
    /**
     * Create bucket with appropriate limits based on type
     */
    private Bucket createBucketForType(RateLimitType rateLimitType) {
        switch (rateLimitType) {
            case CHAT_API:
                return createChatApiBucket();
            case GENERAL_API:
                return createGeneralApiBucket();
            case EXPENSIVE_API:
                return createExpensiveApiBucket();
            case GUEST_USER:
                return createGuestUserBucket();
            case AUTH_USER:
                return createAuthUserBucket();
            default:
                return createDefaultBucket();
        }
    }
    
    /**
     * Chat API bucket: 10 requests per minute, 100 per hour
     */
    private Bucket createChatApiBucket() {
        Bandwidth minuteLimit = Bandwidth.classic(10, Refill.greedy(10, Duration.ofMinutes(1)));
        Bandwidth hourLimit = Bandwidth.classic(100, Refill.greedy(100, Duration.ofHours(1)));
        
        return Bucket.builder()
            .addLimit(minuteLimit)
            .addLimit(hourLimit)
            .build();
    }
    
    /**
     * General API bucket: 30 requests per minute, 500 per hour
     */
    private Bucket createGeneralApiBucket() {
        Bandwidth minuteLimit = Bandwidth.classic(30, Refill.greedy(30, Duration.ofMinutes(1)));
        Bandwidth hourLimit = Bandwidth.classic(500, Refill.greedy(500, Duration.ofHours(1)));
        
        return Bucket.builder()
            .addLimit(minuteLimit)
            .addLimit(hourLimit)
            .build();
    }
    
    /**
     * Expensive API bucket: 3 requests per minute, 20 per hour
     */
    private Bucket createExpensiveApiBucket() {
        Bandwidth minuteLimit = Bandwidth.classic(3, Refill.greedy(3, Duration.ofMinutes(1)));
        Bandwidth hourLimit = Bandwidth.classic(20, Refill.greedy(20, Duration.ofHours(1)));
        
        return Bucket.builder()
            .addLimit(minuteLimit)
            .addLimit(hourLimit)
            .build();
    }
    
    /**
     * Guest user bucket: 5 requests per minute, 50 per hour
     */
    private Bucket createGuestUserBucket() {
        Bandwidth minuteLimit = Bandwidth.classic(5, Refill.greedy(5, Duration.ofMinutes(1)));
        Bandwidth hourLimit = Bandwidth.classic(50, Refill.greedy(50, Duration.ofHours(1)));
        
        return Bucket.builder()
            .addLimit(minuteLimit)
            .addLimit(hourLimit)
            .build();
    }
    
    /**
     * Authenticated user bucket: 20 requests per minute, 200 per hour
     */
    private Bucket createAuthUserBucket() {
        Bandwidth minuteLimit = Bandwidth.classic(20, Refill.greedy(20, Duration.ofMinutes(1)));
        Bandwidth hourLimit = Bandwidth.classic(200, Refill.greedy(200, Duration.ofHours(1)));
        
        return Bucket.builder()
            .addLimit(minuteLimit)
            .addLimit(hourLimit)
            .build();
    }
    
    /**
     * Default bucket: Conservative limits
     */
    private Bucket createDefaultBucket() {
        Bandwidth minuteLimit = Bandwidth.classic(5, Refill.greedy(5, Duration.ofMinutes(1)));
        Bandwidth hourLimit = Bandwidth.classic(50, Refill.greedy(50, Duration.ofHours(1)));
        
        return Bucket.builder()
            .addLimit(minuteLimit)
            .addLimit(hourLimit)
            .build();
    }
    
    /**
     * Clear bucket for user (useful for testing or admin reset)
     */
    public void clearUserBuckets(String userId) {
        userBuckets.entrySet().removeIf(entry -> entry.getKey().startsWith(userId + ":"));
        logger.info("Cleared rate limit buckets for user: {}", userId);
    }
    
    /**
     * Get current bucket stats for monitoring
     */
    public int getCurrentUserCount() {
        return userBuckets.size();
    }
}