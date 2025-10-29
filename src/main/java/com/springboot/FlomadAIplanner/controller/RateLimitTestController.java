package com.springboot.FlomadAIplanner.controller;

import com.springboot.FlomadAIplanner.service.ratelimit.RateLimitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Controller for testing and monitoring rate limiting functionality
 */
@RestController
@RequestMapping("/api/rate-limit")
public class RateLimitTestController {
    
    @Autowired
    private RateLimitService rateLimitService;
    
    /**
     * Test endpoint to verify rate limiting is working
     */
    @GetMapping("/test")
    public ResponseEntity<Map<String, Object>> testRateLimit() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Rate limiting test endpoint");
        response.put("timestamp", System.currentTimeMillis());
        response.put("status", "success");
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get rate limit status for current user
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getRateLimitStatus(
            @RequestParam(defaultValue = "GENERAL_API") String rateLimitType,
            @RequestParam(defaultValue = "anonymous") String userId) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            RateLimitService.RateLimitType type = RateLimitService.RateLimitType.valueOf(rateLimitType);
            long remainingTokens = rateLimitService.getRemainingTokens(userId, type);
            boolean allowed = rateLimitService.isAllowed(userId, type);
            
            response.put("userId", userId);
            response.put("rateLimitType", rateLimitType);
            response.put("remainingTokens", remainingTokens);
            response.put("requestAllowed", allowed);
            response.put("timestamp", System.currentTimeMillis());
            
        } catch (IllegalArgumentException e) {
            response.put("error", "Invalid rate limit type: " + rateLimitType);
            response.put("validTypes", RateLimitService.RateLimitType.values());
        }
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Admin endpoint to clear rate limits for a user
     */
    @DeleteMapping("/clear/{userId}")
    public ResponseEntity<Map<String, Object>> clearUserRateLimit(@PathVariable String userId) {
        rateLimitService.clearUserBuckets(userId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Rate limit buckets cleared for user: " + userId);
        response.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get overall rate limiting stats
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getRateLimitStats() {
        Map<String, Object> response = new HashMap<>();
        response.put("activeBuckets", rateLimitService.getCurrentUserCount());
        response.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.ok(response);
    }
}