package com.springboot.FlomadAIplanner.service.geoapify;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
public class GeoapifyService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${geoapify.api.key}")
    private String apiKey;

    @Value("${geoapify.api.baseurl:https://api.geoapify.com}")
    private String baseUrl;

    @Value("${geoapify.places.cache.ttl-minutes:10}")
    private int cacheTtlMinutes;

    @Value("${geoapify.places.cache.max-size:1000}")
    private int maxCacheSize;

    private final ConcurrentHashMap<String, CacheEntry> cache = new ConcurrentHashMap<>();
    private final Semaphore rateLimiter = new Semaphore(5);
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "geoapify-rate-replenish");
        t.setDaemon(true);
        return t;
    });

    public GeoapifyService(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        scheduler.scheduleAtFixedRate(() -> {
            int permits = 5 - rateLimiter.availablePermits();
            if (permits > 0) {
                rateLimiter.release(permits);
            }
        }, 1, 1, TimeUnit.SECONDS);
        log.info("GeoapifyService initialized");
    }

    public JsonNode geocode(String text) {
        String cacheKey = "geocode:" + text;
        JsonNode cached = getFromCache(cacheKey);
        if (cached != null) return cached;

        try {
            acquirePermit();
            String url = UriComponentsBuilder.fromUriString(baseUrl + "/v1/geocode/search")
                    .queryParam("text", text)
                    .queryParam("apiKey", apiKey)
                    .queryParam("format", "json")
                    .toUriString();

            log.info("Geoapify geocode request for: {}", text);
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            JsonNode result = objectMapper.readTree(response.getBody());
            putInCache(cacheKey, result);
            return result;
        } catch (Exception e) {
            log.error("Geoapify geocode failed for '{}': {}", text, e.getMessage(), e);
            return objectMapper.createObjectNode().put("error", "Geocoding failed: " + e.getMessage());
        }
    }

    public JsonNode reverseGeocode(double lat, double lon) {
        String cacheKey = "reverse:" + lat + "," + lon;
        JsonNode cached = getFromCache(cacheKey);
        if (cached != null) return cached;

        try {
            acquirePermit();
            String url = UriComponentsBuilder.fromUriString(baseUrl + "/v1/geocode/reverse")
                    .queryParam("lat", lat)
                    .queryParam("lon", lon)
                    .queryParam("apiKey", apiKey)
                    .queryParam("format", "json")
                    .toUriString();

            log.info("Geoapify reverse geocode for: {},{}", lat, lon);
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            JsonNode result = objectMapper.readTree(response.getBody());
            putInCache(cacheKey, result);
            return result;
        } catch (Exception e) {
            log.error("Geoapify reverse geocode failed: {}", e.getMessage(), e);
            return objectMapper.createObjectNode().put("error", "Reverse geocoding failed: " + e.getMessage());
        }
    }

    public JsonNode autocomplete(String text, Double biasLat, Double biasLon) {
        String cacheKey = "autocomplete:" + text + ":" + biasLat + ":" + biasLon;
        JsonNode cached = getFromCache(cacheKey);
        if (cached != null) return cached;

        try {
            acquirePermit();
            UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(baseUrl + "/v1/geocode/autocomplete")
                    .queryParam("text", text)
                    .queryParam("apiKey", apiKey)
                    .queryParam("format", "json");

            if (biasLat != null && biasLon != null) {
                builder.queryParam("bias", "proximity:" + biasLon + "," + biasLat);
            }

            String url = builder.toUriString();
            log.info("Geoapify autocomplete for: {}", text);
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            JsonNode result = objectMapper.readTree(response.getBody());
            putInCache(cacheKey, result);
            return result;
        } catch (Exception e) {
            log.error("Geoapify autocomplete failed for '{}': {}", text, e.getMessage(), e);
            return objectMapper.createObjectNode().put("error", "Autocomplete failed: " + e.getMessage());
        }
    }

    public JsonNode searchNearbyPlaces(double lat, double lon, double radiusMeters, String categories) {
        String cacheKey = "places:" + lat + "," + lon + ":" + radiusMeters + ":" + categories;
        JsonNode cached = getFromCache(cacheKey);
        if (cached != null) return cached;

        try {
            acquirePermit();
            UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(baseUrl + "/v2/places")
                    .queryParam("filter", "circle:" + lon + "," + lat + "," + (int) radiusMeters)
                    .queryParam("bias", "proximity:" + lon + "," + lat)
                    .queryParam("limit", 20)
                    .queryParam("apiKey", apiKey);

            if (categories != null && !categories.isEmpty()) {
                builder.queryParam("categories", categories);
            }

            String url = builder.toUriString();
            log.info("Geoapify places search near {},{} radius={} categories={}", lat, lon, radiusMeters, categories);
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            JsonNode result = objectMapper.readTree(response.getBody());
            putInCache(cacheKey, result);
            return result;
        } catch (Exception e) {
            log.error("Geoapify places search failed: {}", e.getMessage(), e);
            return objectMapper.createObjectNode().put("error", "Places search failed: " + e.getMessage());
        }
    }

    public JsonNode textSearchPlaces(String text) {
        String cacheKey = "textsearch:" + text;
        JsonNode cached = getFromCache(cacheKey);
        if (cached != null) return cached;

        try {
            JsonNode geocodeResult = geocode(text);

            if (geocodeResult.has("results") && geocodeResult.get("results").size() > 0) {
                JsonNode firstResult = geocodeResult.get("results").get(0);
                double lat = firstResult.get("lat").asDouble();
                double lon = firstResult.get("lon").asDouble();

                JsonNode placesResult = searchNearbyPlaces(lat, lon, 500, null);

                com.fasterxml.jackson.databind.node.ObjectNode combined = objectMapper.createObjectNode();
                combined.set("geocode", firstResult);
                combined.set("nearbyPlaces", placesResult);
                putInCache(cacheKey, combined);
                return combined;
            }

            putInCache(cacheKey, geocodeResult);
            return geocodeResult;
        } catch (Exception e) {
            log.error("Geoapify text search failed for '{}': {}", text, e.getMessage(), e);
            return objectMapper.createObjectNode().put("error", "Text search failed: " + e.getMessage());
        }
    }

    public JsonNode getDirections(String waypoints, String mode) {
        String cacheKey = "directions:" + waypoints + ":" + mode;
        JsonNode cached = getFromCache(cacheKey);
        if (cached != null) return cached;

        try {
            acquirePermit();
            String url = UriComponentsBuilder.fromUriString(baseUrl + "/v1/routing")
                    .queryParam("waypoints", waypoints)
                    .queryParam("mode", mode != null ? mode : "drive")
                    .queryParam("apiKey", apiKey)
                    .toUriString();

            log.info("Geoapify routing request: waypoints={} mode={}", waypoints, mode);
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            JsonNode result = objectMapper.readTree(response.getBody());
            putInCache(cacheKey, result);
            return result;
        } catch (Exception e) {
            log.error("Geoapify routing failed: {}", e.getMessage(), e);
            return objectMapper.createObjectNode().put("error", "Routing failed: " + e.getMessage());
        }
    }

    public JsonNode getRouteMatrix(List<double[]> points, String mode) {
        String cacheKey = "matrix:" + points.stream()
                .map(p -> p[0] + "," + p[1])
                .collect(Collectors.joining("|")) + ":" + mode;
        JsonNode cached = getFromCache(cacheKey);
        if (cached != null) return cached;

        try {
            acquirePermit();
            String url = UriComponentsBuilder.fromUriString(baseUrl + "/v1/routematrix")
                    .queryParam("mode", mode != null ? mode : "drive")
                    .queryParam("apiKey", apiKey)
                    .toUriString();

            com.fasterxml.jackson.databind.node.ObjectNode requestBody = objectMapper.createObjectNode();
            com.fasterxml.jackson.databind.node.ArrayNode sourcesArray = requestBody.putArray("sources");
            com.fasterxml.jackson.databind.node.ArrayNode targetsArray = requestBody.putArray("targets");
            for (double[] point : points) {
                com.fasterxml.jackson.databind.node.ObjectNode loc = objectMapper.createObjectNode();
                loc.putArray("location").add(point[1]).add(point[0]);
                sourcesArray.add(loc.deepCopy());
                targetsArray.add(loc.deepCopy());
            }

            log.info("Geoapify route matrix request: {} points, mode={}", points.size(), mode);
            ResponseEntity<String> response = restTemplate.postForEntity(url, requestBody, String.class);
            JsonNode result = objectMapper.readTree(response.getBody());
            putInCache(cacheKey, result);
            return result;
        } catch (Exception e) {
            log.error("Geoapify route matrix failed: {}", e.getMessage(), e);
            return objectMapper.createObjectNode().put("error", "Route matrix failed: " + e.getMessage());
        }
    }

    private void acquirePermit() {
        try {
            if (!rateLimiter.tryAcquire(5, TimeUnit.SECONDS)) {
                log.warn("Geoapify rate limit reached, request may be delayed");
                rateLimiter.acquire();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Rate limiter interrupted", e);
        }
    }

    private JsonNode getFromCache(String key) {
        CacheEntry entry = cache.get(key);
        if (entry != null && !entry.isExpired(cacheTtlMinutes)) {
            log.debug("Cache hit for key: {}", key);
            return entry.value;
        }
        if (entry != null) {
            cache.remove(key);
        }
        return null;
    }

    private void putInCache(String key, JsonNode value) {
        if (cache.size() >= maxCacheSize) {
            cache.entrySet().removeIf(e -> e.getValue().isExpired(cacheTtlMinutes));
            if (cache.size() >= maxCacheSize) {
                String oldestKey = cache.entrySet().stream()
                        .min((a, b) -> Long.compare(a.getValue().timestamp, b.getValue().timestamp))
                        .map(java.util.Map.Entry::getKey)
                        .orElse(null);
                if (oldestKey != null) {
                    cache.remove(oldestKey);
                }
            }
        }
        cache.put(key, new CacheEntry(value));
    }

    private static class CacheEntry {
        final JsonNode value;
        final long timestamp;

        CacheEntry(JsonNode value) {
            this.value = value;
            this.timestamp = System.currentTimeMillis();
        }

        boolean isExpired(int ttlMinutes) {
            return System.currentTimeMillis() - timestamp > ttlMinutes * 60_000L;
        }
    }
}
