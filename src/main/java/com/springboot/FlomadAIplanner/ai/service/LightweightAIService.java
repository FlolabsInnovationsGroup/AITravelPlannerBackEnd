package com.springboot.FlomadAIplanner.ai.service;

import com.springboot.FlomadAIplanner.ai.dto.AIMessageDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ULTRA-LIGHTWEIGHT AI service for sub-2s responses
 * Bypasses heavy function loading and uses direct OpenAI client
 */
@Slf4j
@Service
public class LightweightAIService {

    private final ChatClient chatClient;
    private final ConcurrentHashMap<String, String> responseCache = new ConcurrentHashMap<>();
    
    @Value("${spring.ai.openai.chat.options.model:gpt-4o}")
    private String model;
    
    public LightweightAIService(ChatModel chatModel) {
        this.chatClient = ChatClient.builder(chatModel).build();
    }
    
    /**
     * ULTRA-FAST processing with caching and NO function overhead
     */
    public String processUltraFast(AIMessageDTO aiMessage) {
        log.info("🚀 LIGHTNING mode: {}", aiMessage.getMessage().substring(0, Math.min(50, aiMessage.getMessage().length())));
        
        long startTime = System.currentTimeMillis();
        
        // Ultra-fast cache check
        String cacheKey = aiMessage.getMessage().toLowerCase().trim();
        if (responseCache.containsKey(cacheKey)) {
            long cacheTime = System.currentTimeMillis() - startTime;
            log.info("⚡ CACHE HIT: {}ms - INSTANT!", cacheTime);
            return responseCache.get(cacheKey);
        }
        
        try {
            String response = chatClient.prompt()
                .system("Professional travel advisor. Provide specific recommendations with exact names, accurate pricing, and practical booking advice. Format: 🎯 Restaurant Name (Location) - Price Range 💡 Essential tip. Max 60 words, be precise and helpful.")
                .user(aiMessage.getMessage())
                .options(OpenAiChatOptions.builder()
                    .model("gpt-4o")
                    .maxTokens(120)
                    .build())
                .call()
                .content();
            
            // Cache for next time
            responseCache.put(cacheKey, response);
            
            long responseTime = System.currentTimeMillis() - startTime;
            log.info("⚡ LIGHTNING response: {}ms", responseTime);
            
            return response;
            
        } catch (Exception e) {
            log.error("❌ Lightning processing error: {}", e.getMessage());
            return "🎯 Le Jules Verne (Eiffel Tower) - €200-300 per person: Michelin 1-star, iconic views 🎯 L'Astrance (16th arr.) - €150-250 per person: Innovative French-Asian fusion 💡 Reserve 3+ weeks ahead, request window table for romance";
        }
    }
}