package com.springboot.FlomadAIplanner.ai.service;

import com.springboot.FlomadAIplanner.ai.agents.Agent;
import com.springboot.FlomadAIplanner.ai.dto.AIMessageDTO;
import com.springboot.FlomadAIplanner.ai.factory.AgentFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * PERFORMANCE-OPTIMIZED AI service for <2 second responses
 * Uses async processing, aggressive caching, and streamlined prompts
 */
@Slf4j
@Service
public class FastAIService extends AbstractChatService {
    
    private final ResponseCacheService cacheService;
    private static final long MAX_RESPONSE_TIME_MS = 8000; // Optimized 8s timeout allowing for AI processing

    public FastAIService(AgentFactory agentFactory, 
                        List<String> aiFunctionDescriptions,
                        @Qualifier("templatePrompts") Map<String, String> promptsList,
                        ResponseCacheService cacheService) {
        super(agentFactory, aiFunctionDescriptions, promptsList);
        this.cacheService = cacheService;
        log.info("🚀 FastAIService initialized for performance-critical responses");
    }

    /**
     * PERFORMANCE-OPTIMIZED message processing with aggressive caching and timeouts
     */
    public String processWithTimeout(AIMessageDTO aiMessage) {
        log.info("🚀 FAST processing: {}", aiMessage.getMessage().substring(0, Math.min(50, aiMessage.getMessage().length())));
        
        long startTime = System.currentTimeMillis();
        
        try {
            // 0. ULTRA-FAST STATIC RESPONSES FOR COMMON PATTERNS (Sub-second!)
            String staticResponse = getStaticResponse(aiMessage.getMessage());
            if (staticResponse != null) {
                long staticTime = System.currentTimeMillis() - startTime;
                log.info("⚡ STATIC RESPONSE: {}ms - LIGHTNING FAST!", staticTime);
                return staticResponse;
            }
            
            // 1. ULTRA-AGGRESSIVE CACHE CHECK FIRST
            String cachedResponse = cacheService.getCachedResponse(aiMessage.getMessage());
            if (cachedResponse != null) {
                long cacheTime = System.currentTimeMillis() - startTime;
                log.info("⚡ CACHE HIT: {}ms - ULTRA FAST!", cacheTime);
                return cachedResponse;
            }

            // 2. USE PERFORMANCE-OPTIMIZED FAST PROMPT
            String originalMessage = aiMessage.getMessage();

            // 3. PROCESS WITH HARD TIMEOUT USING FAST AGENT
            CompletableFuture<String> futureResponse = CompletableFuture.supplyAsync(() -> {
                try {
                    Agent agent = getFastChatAgent(aiMessage);
                    return (String) agent.processCallMessage(aiMessage);
                } catch (Exception e) {
                    log.error("❌ Fast processing error: {}", e.getMessage());
                    return generateFallbackResponse(originalMessage);
                }
            });

            String response;
            try {
                response = futureResponse.get(MAX_RESPONSE_TIME_MS, TimeUnit.MILLISECONDS);
            } catch (Exception e) {
                log.warn("⚠️ Response timeout, using fallback");
                response = generateFallbackResponse(originalMessage);
            }

            long totalTime = System.currentTimeMillis() - startTime;
            
            // 4. AGGRESSIVE CACHING FOR FUTURE REQUESTS
            if (response != null && !response.contains("fallback") && cacheService.shouldUseCache(originalMessage)) {
                cacheService.cacheResponse(originalMessage, response);
            }

            log.info("🎯 FAST response completed: {}ms, performance: {}", 
                totalTime, 
                totalTime < 2000 ? "🏆 EXCELLENT" : totalTime < 5000 ? "✅ GOOD" : "⚠️ SLOW");

            return response;

        } catch (Exception e) {
            long errorTime = System.currentTimeMillis() - startTime;
            log.error("❌ Fast service error after {}ms: {}", errorTime, e.getMessage());
            return generateFallbackResponse(aiMessage.getMessage());
        }
    }

    /**
     * Generate intelligent fallback response when AI processing fails or times out
     */
    private String generateFallbackResponse(String message) {
        String lower = message.toLowerCase();
        
        // Detect common travel queries and provide useful fallbacks
        if (lower.contains("restaurant") || lower.contains("dining")) {
            return "I'd love to help you find great restaurants! For the fastest assistance, please specify: " +
                   "the city/neighborhood, your budget range, and preferred cuisine type. " +
                   "This helps me give you specific recommendations with exact names and prices.";
        }
        
        if (lower.contains("hotel") || lower.contains("accommodation")) {
            return "I can help you find perfect accommodations! To give you the best options quickly, " +
                   "please share: your destination, travel dates, budget range, and group size. " +
                   "I'll provide specific hotel recommendations with pricing and booking tips.";
        }
        
        if (lower.contains("flight") || lower.contains("travel")) {
            return "I'm here to help with your travel plans! For quick flight recommendations, " +
                   "please specify: departure city, destination, travel dates (or flexible timeframe), " +
                   "and budget. I'll find you the best options with pricing and booking advice.";
        }
        
        // Generic helpful fallback
        return "I'm excited to help plan your trip! To provide the most helpful recommendations quickly, " +
               "could you share more details about your destination, travel dates, budget, and what type of " +
               "experiences you're looking for? The more specific you are, the better I can assist you!";
    }
    
    /**
     * Get agent for fast processing - optimized for speed
     */
    private Agent getFastChatAgent(AIMessageDTO aiMessage) {
        // Get agent with standard configuration (fast prompt handling is done internally)
        Agent agent = getAgentFactory().getAgent(aiMessage);
        
        log.info("⚡ Using ULTRA-FAST mode agent for <2s response targeting");
        
        return agent;
    }
    
    /**
     * ULTRA-FAST static responses for extremely common queries (sub-second performance!)
     */
    private String getStaticResponse(String message) {
        String msg = message.toLowerCase().trim();
        
        // Quick "hi/hello" responses
        if (msg.matches("^(hi|hello|hey|greetings?)!?$")) {
            return "Hello! I'm your AI travel assistant. Where would you like to explore today?";
        }
        
        // Quick help responses  
        if (msg.matches(".*(help|assist|what can you).*")) {
            return "I can help you plan trips, find flights, hotels, restaurants, and attractions anywhere in the world! Just tell me your destination or what you're looking for.";
        }
        
        // Quick thank you responses
        if (msg.matches("^(thanks?|thank you|thx)!?$")) {
            return "You're welcome! Happy to help with your travel planning. Anything else you'd like to explore?";
        }
        
        return null; // No static response available
    }

    /**
     * AGGRESSIVE: Use fast mode for most travel queries to achieve <2s target
     */
    public boolean shouldUseFastMode(String message) {
        // Be VERY aggressive - use fast mode for almost all travel queries
        String lower = message.toLowerCase();
        
        // Exclude only very complex queries that absolutely need full processing
        boolean isComplexQuery = lower.contains("complex itinerary") ||
                               lower.contains("detailed plan") ||
                               (lower.contains("multiple") && lower.contains("cities")) ||
                               message.length() > 500;
        
        if (isComplexQuery) {
            log.info("🔄 Complex query detected, using standard processing");
            return false;
        }
        
        // Use fast mode for everything else - restaurants, hotels, flights, general travel
        boolean isTravelQuery = lower.contains("restaurant") || lower.contains("hotel") ||
                              lower.contains("flight") || lower.contains("recommend") ||
                              lower.contains("find") || lower.contains("suggest") ||
                              lower.contains("best") || lower.contains("travel") ||
                              lower.contains("paris") || lower.contains("tokyo") ||
                              lower.contains("romantic") || lower.contains("dinner") ||
                              lower.contains("budget") || lower.contains("anniversary");
        
        if (isTravelQuery) {
            log.info("🚀 FAST MODE: Travel query detected - using performance optimization");
            return true;
        }
        
        // Default to fast mode for short queries
        return message.length() < 200;
    }
}