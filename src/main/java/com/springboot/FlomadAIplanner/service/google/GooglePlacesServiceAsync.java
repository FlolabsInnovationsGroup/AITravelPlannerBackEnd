package com.springboot.FlomadAIplanner.service.google;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.springboot.FlomadAIplanner.dto.google.NearbyPlacesRequest;
import com.springboot.FlomadAIplanner.dto.google.TextSearchRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service class for handling Google Places API interactions asynchronously with smart caching.
 */
@Slf4j
@Service
public class GooglePlacesServiceAsync {

    private final GooglePlacesClient googlePlacesClient;

    @Autowired(required = false)
    private GooglePlacesMetricsService metricsService;

    @Value("${google.api.key}")
    private String googleApiKey;

    @Value("${google.places.fieldMask}")
    private String placesFieldMask;

    @Value("${google.textSearch.fieldMask}")
    private String textSearchFieldMask;

    @Value("${google.places.cache.ttl-minutes:5}")
    private int cacheTtlMinutes;

    @Value("${google.places.cache.max-size:1000}")
    private int maxCacheSize;

    // In-memory cache for text search results (saves $30-60/month)
    private final ConcurrentHashMap<String, CacheEntry> textSearchCache = new ConcurrentHashMap<>();
    
    // Metrics counters
    private long cacheHits = 0;
    private long cacheMisses = 0;
    private long totalApiCalls = 0;

    public GooglePlacesServiceAsync(GooglePlacesClient googlePlacesClient) {
        this.googlePlacesClient = googlePlacesClient;
    }

    /**
     * Asynchronously performs a search for nearby places using the provided request.
     *
     * @param request The request object containing search parameters.
     * @return a CompletableFuture containing the JSON response from the search.
     */
    @Async
    public CompletableFuture<JsonNode> searchNearbyPlaces(NearbyPlacesRequest request) {
        log.info("Searching Google nearby places with request: {}", request);
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Track API call for cost monitoring
                if (metricsService != null) {
                    metricsService.recordPlaceDetails();
                }
                
                String location = String.format("%f,%f", 
                    request.getLocationRestriction().getCircle().getCenter().getLatitude(),
                    request.getLocationRestriction().getCircle().getCenter().getLongitude());
                int radius = (int) request.getLocationRestriction().getCircle().getRadius();
                String type = String.join("|", request.getIncludedTypes());
                
                return googlePlacesClient.searchNearbyPlaces(
                        googleApiKey,              // API key as query parameter
                        location,                  // Latitude,longitude
                        radius,                    // Search radius in meters
                        type,                      // Place types
                        placesFieldMask            // Field mask
                );
            } catch (Exception e) {
                log.error("Error while searching nearby places", e);
                return JsonNodeFactory.instance.objectNode().put("error", e.getMessage());
            }
        });
    }

    /**
     * Asynchronously performs a text search for places with intelligent caching.
     * Cache saves ~$30-60/month by eliminating duplicate API calls.
     *
     * @param request The request object containing the text query and other parameters.
     * @return a CompletableFuture containing the JSON response from the search.
     */
    @Async
    public CompletableFuture<JsonNode> asyncTextSearchPlaces(TextSearchRequest request) {
        String cacheKey = generateCacheKey(request.getTextQuery());
        
        // Check cache first
        CacheEntry cached = textSearchCache.get(cacheKey);
        if (cached != null && !cached.isExpired()) {
            cacheHits++;
            log.info("🎯 CACHE HIT for text search: '{}' (Hits: {}, Misses: {}, Hit Rate: {:.1f}%)", 
                    request.getTextQuery(), cacheHits, cacheMisses, getCacheHitRate());
            return CompletableFuture.completedFuture(cached.getResult());
        }
        
        // Cache miss - call API
        cacheMisses++;
        log.info("📡 CACHE MISS - Calling Google API for: '{}' (Hits: {}, Misses: {})", 
                request.getTextQuery(), cacheHits, cacheMisses);
        
        return CompletableFuture.supplyAsync(() -> {
            try {
                totalApiCalls++;
                if (metricsService != null) {
                    metricsService.recordTextSearch();
                }
                
                JsonNode result = googlePlacesClient.textSearchPlaces(
                        textSearchFieldMask,
                        request
                );
                
                // Cache the result
                long expiresAt = System.currentTimeMillis() + (cacheTtlMinutes * 60 * 1000L);
                textSearchCache.put(cacheKey, new CacheEntry(result, expiresAt));
                cleanupCacheIfNeeded();
                
                log.debug("✅ Cached result for: '{}' (TTL: {} min, Cache size: {})", 
                         request.getTextQuery(), cacheTtlMinutes, textSearchCache.size());
                
                return result;
            } catch (Exception e) {
                log.error("Error performing text search for query: {}", request.getTextQuery(), e);
                return JsonNodeFactory.instance.objectNode().put("error", e.getMessage());
            }
        });
    }

    /**
     * Generate normalized cache key from search query.
     */
    private String generateCacheKey(String query) {
        if (query == null) return "";
        return query.toLowerCase().trim().replaceAll("\\s+", " ");
    }

    /**
     * Clean up expired cache entries and enforce size limit.
     */
    private void cleanupCacheIfNeeded() {
        // Remove expired entries
        long now = System.currentTimeMillis();
        textSearchCache.entrySet().removeIf(entry -> entry.getValue().isExpired());
        
        // Enforce max cache size by removing oldest entries
        if (textSearchCache.size() > maxCacheSize) {
            int removeCount = maxCacheSize / 5; // Remove oldest 20%
            textSearchCache.entrySet().stream()
                .limit(removeCount)
                .forEach(entry -> textSearchCache.remove(entry.getKey()));
            log.info("🧹 Cache cleanup: removed {} entries (current size: {})", removeCount, textSearchCache.size());
        }
    }

    /**
     * Get current cache hit rate percentage.
     */
    private double getCacheHitRate() {
        long total = cacheHits + cacheMisses;
        return total > 0 ? (cacheHits * 100.0 / total) : 0;
    }

    /**
     * Get cache metrics for monitoring.
     */
    public String getCacheMetrics() {
        return String.format(
            "Cache Stats - Hits: %d, Misses: %d, Hit Rate: %.1f%%, Size: %d, API Calls: %d",
            cacheHits, cacheMisses, getCacheHitRate(), textSearchCache.size(), totalApiCalls
        );
    }

    /**
     * Cache entry with expiration timestamp.
     */
    private static class CacheEntry {
        private final JsonNode result;
        private final long expiresAt;

        public CacheEntry(JsonNode result, long expiresAt) {
            this.result = result;
            this.expiresAt = expiresAt;
        }

        public JsonNode getResult() {
            return result;
        }

        public boolean isExpired() {
            return System.currentTimeMillis() > expiresAt;
        }
    }
}
