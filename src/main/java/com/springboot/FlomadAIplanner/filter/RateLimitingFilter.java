package com.springboot.FlomadAIplanner.filter;

import com.springboot.FlomadAIplanner.service.ratelimit.RateLimitService;
import com.springboot.FlomadAIplanner.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Rate limiting filter that applies rate limits to incoming requests.
 * Uses Bucket4j token bucket algorithm for different user types and endpoints.
 */
public class RateLimitingFilter implements Filter {
    
    private static final Logger logger = LoggerFactory.getLogger(RateLimitingFilter.class);
    
    private final RateLimitService rateLimitService;
    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    public RateLimitingFilter(RateLimitService rateLimitService, JwtUtil jwtUtil) {
        this.rateLimitService = rateLimitService;
        this.jwtUtil = jwtUtil;
    }
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) 
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        // Skip rate limiting for certain endpoints
        if (shouldSkipRateLimit(httpRequest)) {
            chain.doFilter(request, response);
            return;
        }
        
        try {
            // Determine user ID and type
            String userId = extractUserId(httpRequest);
            RateLimitService.RateLimitType rateLimitType = determineRateLimitType(httpRequest, userId);
            
            // Check rate limit
            if (!rateLimitService.isAllowed(userId, rateLimitType)) {
                handleRateLimitExceeded(httpResponse, rateLimitType);
                return;
            }
            
            // Add rate limit headers to response
            addRateLimitHeaders(httpResponse, userId, rateLimitType);
            
            // Continue with request
            chain.doFilter(request, response);
            
        } catch (Exception e) {
            logger.error("Error in rate limiting filter", e);
            // If rate limiting fails, continue with request to avoid blocking
            chain.doFilter(request, response);
        }
    }
    
    /**
     * Check if request should skip rate limiting
     */
    private boolean shouldSkipRateLimit(HttpServletRequest request) {
        String path = request.getRequestURI();
        String method = request.getMethod();
        
        // Skip for health checks, actuator endpoints, static resources
        return path.startsWith("/actuator") ||
               path.startsWith("/swagger") ||
               path.startsWith("/v3/api-docs") ||
               path.startsWith("/webjars") ||
               path.equals("/") ||
               path.equals("/health") ||
               (path.startsWith("/api/auth") && "POST".equals(method) && path.contains("signin")) ||
               path.contains("static") ||
               path.endsWith(".css") ||
               path.endsWith(".js") ||
               path.endsWith(".ico");
    }
    
    /**
     * Extract user ID from request
     */
    private String extractUserId(HttpServletRequest request) {
        try {
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                String username = jwtUtil.extractUsername(token);
                return username != null ? username : getClientIpAddress(request);
            }
        } catch (Exception e) {
            logger.debug("Could not extract user ID from token, using IP address", e);
        }
        
        return getClientIpAddress(request);
    }
    
    /**
     * Get client IP address for identification
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
    
    /**
     * Determine rate limit type based on request path and user
     */
    private RateLimitService.RateLimitType determineRateLimitType(HttpServletRequest request, String userId) {
        String path = request.getRequestURI();
        boolean isAuthenticated = isUserAuthenticated(request);
        
        // AI/Chat endpoints - most restrictive
        if (path.startsWith("/api/chat") || path.contains("chat")) {
            return isAuthenticated ? RateLimitService.RateLimitType.CHAT_API : RateLimitService.RateLimitType.GUEST_USER;
        }
        
        // Expensive AI operations
        if (path.contains("amadeus") || path.contains("places") || path.contains("viator")) {
            return RateLimitService.RateLimitType.EXPENSIVE_API;
        }
        
        // User type based limits
        if (!isAuthenticated) {
            return RateLimitService.RateLimitType.GUEST_USER;
        }
        
        // Default for authenticated users
        return RateLimitService.RateLimitType.AUTH_USER;
    }
    
    /**
     * Check if user is authenticated
     */
    private boolean isUserAuthenticated(HttpServletRequest request) {
        try {
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                try {
                    String username = jwtUtil.extractUsername(token);
                    // Guest users have username starting with "guest_"
                    boolean isGuest = username != null && username.startsWith("guest_");
                    return !isGuest && !jwtUtil.extractExpiration(token).after(new Date());
                } catch (Exception e) {
                    return false;
                }
            }
        } catch (Exception e) {
            logger.debug("Error checking authentication", e);
        }
        return false;
    }
    
    /**
     * Handle rate limit exceeded
     */
    private void handleRateLimitExceeded(HttpServletResponse response, RateLimitService.RateLimitType rateLimitType) 
            throws IOException {
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType("application/json");
        
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("error", "Rate limit exceeded");
        errorResponse.put("message", "Too many requests. Please try again later.");
        errorResponse.put("rateLimitType", rateLimitType.name());
        errorResponse.put("status", HttpStatus.TOO_MANY_REQUESTS.value());
        
        String jsonResponse = objectMapper.writeValueAsString(errorResponse);
        response.getWriter().write(jsonResponse);
        
        logger.warn("Rate limit exceeded for type: {}", rateLimitType);
    }
    
    /**
     * Add rate limit headers to response
     */
    private void addRateLimitHeaders(HttpServletResponse response, String userId, RateLimitService.RateLimitType rateLimitType) {
        try {
            long remainingTokens = rateLimitService.getRemainingTokens(userId, rateLimitType);
            response.setHeader("X-RateLimit-Remaining", String.valueOf(remainingTokens));
            response.setHeader("X-RateLimit-Type", rateLimitType.name());
        } catch (Exception e) {
            logger.debug("Error adding rate limit headers", e);
        }
    }
}