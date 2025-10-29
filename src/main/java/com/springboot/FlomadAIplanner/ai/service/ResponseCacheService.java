package com.springboot.FlomadAIplanner.ai.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

/**
 * Simple response caching service to improve AI response performance
 * for frequently asked travel questions
 */
@Slf4j
@Service
public class ResponseCacheService {
    
    private final Map<String, CachedResponse> cache = new ConcurrentHashMap<>();
    private static final long CACHE_TTL_MS = 30 * 60 * 1000; // 30 minutes
    private static final int MAX_CACHE_SIZE = 100;
    
    public static class CachedResponse {
        private final String response;
        private final long timestamp;
        
        public CachedResponse(String response) {
            this.response = response;
            this.timestamp = System.currentTimeMillis();
        }
        
        public String getResponse() { return response; }
        public boolean isExpired() {
            return System.currentTimeMillis() - timestamp > CACHE_TTL_MS;
        }
    }
    
    /**
     * Generate cache key from normalized user message
     */
    public String generateCacheKey(String message) {
        return message.toLowerCase()
                .replaceAll("[^a-z0-9\\s]", "")
                .replaceAll("\\s+", " ")
                .trim();
    }
    
    /**
     * Get cached response if available and not expired
     */
    public String getCachedResponse(String message) {
        String key = generateCacheKey(message);
        CachedResponse cached = cache.get(key);
        
        if (cached != null && !cached.isExpired()) {
            log.info("🚀 Cache HIT for message: {}", message.substring(0, Math.min(50, message.length())));
            return cached.getResponse();
        }
        
        if (cached != null && cached.isExpired()) {
            cache.remove(key);
            log.debug("🗑️ Expired cache entry removed for: {}", key);
        }
        
        // Check for partial matches for common queries
        return findPartialMatch(message);
    }
    
    /**
     * Find partial matches for similar queries to improve cache hit rate
     */
    private String findPartialMatch(String message) {
        String normalized = generateCacheKey(message);
        
        // Check for keyword matches with high-value cached responses
        for (Map.Entry<String, CachedResponse> entry : cache.entrySet()) {
            if (!entry.getValue().isExpired()) {
                String cachedKey = entry.getKey();
                // Simple keyword matching for travel queries
                if (hasCommonKeywords(normalized, cachedKey)) {
                    log.info("🎯 Partial cache match found for: {}", message.substring(0, Math.min(30, message.length())));
                    return entry.getValue().getResponse();
                }
            }
        }
        
        return null;
    }
    
    private boolean hasCommonKeywords(String query1, String query2) {
        String[] words1 = query1.split("\\s+");
        String[] words2 = query2.split("\\s+");
        
        // Check for destination matches
        for (String word1 : words1) {
            if (word1.length() > 4) { // Only check meaningful words
                for (String word2 : words2) {
                    if (word1.equals(word2)) {
                        return true;
                    }
                }
            }
        }
        
        return false;
    }
    
    /**
     * Cache a response if cache size allows
     */
    public void cacheResponse(String message, String response) {
        if (cache.size() >= MAX_CACHE_SIZE) {
            // Simple eviction - remove oldest entries
            cache.entrySet().removeIf(entry -> entry.getValue().isExpired());
            
            // If still at capacity, remove some entries
            if (cache.size() >= MAX_CACHE_SIZE) {
                cache.entrySet().iterator().next();
                cache.entrySet().iterator().remove();
            }
        }
        
        String key = generateCacheKey(message);
        cache.put(key, new CachedResponse(response));
        log.info("💾 Cached response for: {}", message.substring(0, Math.min(50, message.length())));
    }
    
    /**
     * Check if message is similar enough to existing cache keys for potential reuse
     */
    public boolean shouldUseCache(String message) {
        // Don't cache very specific or personal queries
        String lower = message.toLowerCase();
        return !lower.contains("my name") && 
               !lower.contains("i am") && 
               !lower.contains("personal") &&
               message.length() > 10;
    }
    
    /**
     * Get cache statistics
     */
    public Map<String, Object> getCacheStats() {
        long expired = cache.values().stream()
                .mapToLong(cached -> cached.isExpired() ? 1 : 0)
                .sum();
        
        return Map.of(
            "totalEntries", cache.size(),
            "expiredEntries", expired,
            "activeEntries", cache.size() - expired
        );
    }
}