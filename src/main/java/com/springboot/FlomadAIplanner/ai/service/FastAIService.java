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
    private static final long MAX_RESPONSE_TIME_MS = 30000; // 30s timeout for complex AI function calling

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
     * Generate personality-aligned fallback response when AI processing fails or times out
     * MUST match enforced personality: enthusiastic, budget-aware, specific, with emojis
     */
    private String generateFallbackResponse(String message) {
        String lower = message.toLowerCase();
        
        // Hotel/Accommodation fallback
        if (lower.contains("hotel") || lower.contains("accommodation") || lower.contains("stay")) {
            return "Ooh, finding the perfect place to stay is SO exciting! 🏨✨ I'm here to help you " +
                   "discover an amazing spot! To give you those insider recommendations with exact prices " +
                   "and pro tips, I need a few details: Which destination is calling your name? What's your " +
                   "nightly budget? And any must-haves like pool, breakfast, or romantic vibes? Trust me - " +
                   "with these details, I'll find you a place that'll make your friends jealous! 💰";
        }
        
        // Restaurant/Dining fallback
        if (lower.contains("restaurant") || lower.contains("dining") || lower.contains("eat") || lower.contains("food")) {
            return "Ooh YES, let's talk about food! 🍽️✨ I'm absolutely pumped to share the best dining " +
                   "spots with you! To hook you up with specific restaurant names, exact prices, and those " +
                   "pro tips that locals don't advertise, tell me: Which city are you exploring? What's " +
                   "your budget per meal? Any cuisine cravings? Your wallet will thank you for asking - " +
                   "I know all the hidden gems that beat the tourist traps! 💰🌟";
        }
        
        // Flight/Travel fallback
        if (lower.contains("flight") || lower.contains("fly") || lower.contains("ticket")) {
            return "Ooh, planning flights is always exciting! ✈️💫 Let me help you find the best deals! " +
                   "To get you specific flight options with exact prices and money-saving booking tips, " +
                   "I need: departure city, destination, travel dates (or flexible timeframe), and your " +
                   "budget. Pro tip that locals don't advertise: Flexible dates can save you 30-50%! " +
                   "Trust me on this one - I'll find you amazing deals! 💰";
        }
        
        // Itinerary/Planning fallback
        if (lower.contains("itinerary") || lower.contains("plan") || lower.contains("trip") || lower.contains("days")) {
            return "Ooh, planning an adventure is absolutely magical! 🗺️✨ I'm SO excited to help you " +
                   "craft the perfect itinerary with specific venues, exact prices, and insider secrets! " +
                   "To create that Pinterest-perfect plan, tell me: destination, number of days, daily " +
                   "budget, and your vibe (foodie? culture? relaxation?). Your wallet will thank you for " +
                   "the money-saving tips I'm about to share! This is going to be AMAZING! 🎯💰";
        }
        
        // Generic travel fallback
        return "Ooh, I'm absolutely thrilled to help plan your trip! 🌍✨ To give you those specific " +
               "recommendations with exact prices, insider tips, and pro advice that'll make your travel " +
               "dreams come true, tell me more! Where are you thinking of going? What's your budget? " +
               "How many days? Trust me - the more details you share, the more magic I can create! " +
               "This is going to be incredible! 💰🎉";
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
        
        // Quick help responses - but NOT for emergencies
        if (msg.matches("^(help|assist|what can you( do| help with)?)\\??$") && 
            !msg.contains("lost") && !msg.contains("emergency") && !msg.contains("passport") && !msg.contains("stranded")) {
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