package com.springboot.FlomadAIplanner.ai.service;

import com.springboot.FlomadAIplanner.ai.agents.Agent;
import com.springboot.FlomadAIplanner.ai.dto.AIMessageDTO;
import com.springboot.FlomadAIplanner.ai.dto.AIMessageResponse;
import com.springboot.FlomadAIplanner.ai.factory.AgentFactory;
import com.springboot.FlomadAIplanner.ai.functions.AutoLocationExtractionFunction;
import com.springboot.FlomadAIplanner.ai.functions.ShowLocationFunction;
import com.springboot.FlomadAIplanner.ai.validation.AIResponseValidationService;
import com.springboot.FlomadAIplanner.ai.validation.ValidationResult;
import com.springboot.FlomadAIplanner.model.entities.Message;
import com.springboot.FlomadAIplanner.service.geoapify.GeoapifyService;
import com.springboot.FlomadAIplanner.service.model.MessageService;
import lombok.extern.slf4j.Slf4j;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Enhanced ChatCallService that explicitly saves user and assistant messages to database
 * This ensures proper message persistence for chat history functionality.
 */
@Slf4j
@Service
public class EnhancedChatCallService extends AbstractChatService {
    
    private final MessageService messageService;
    private final AIResponseValidationService validationService;
    private final ResponseCacheService cacheService;
    private final AutoLocationExtractionFunction autoLocationExtractionFunction;
    private final GeoapifyService geoapifyService;
    private final ShowLocationFunction showLocationFunction;
    
    // Pattern to detect tour/itinerary requests that should trigger location extraction
    private static final Pattern TOUR_PATTERN = Pattern.compile(
        "\\b(tour|itinerary|trip|journey|visit|travel to|explore|foodie|cultural|day trip|multi-city)\\b",
        Pattern.CASE_INSENSITIVE
    );

    // Single-location “spotlight” prompts that we can deterministically fulfill if the AI didn't call functions.
    // Examples:
    // - "show me Paris"
    // - "take me to London"
    // - "focus on Tokyo"
    private static final Pattern SIMPLE_SPOTLIGHT_PATTERN = Pattern.compile(
        "^\\s*(show\\s+me|take\\s+me\\s+to|focus\\s+on|where\\s+is)\\s+\"?([^\"?\\n]+?)\"?\\s*[\\?\\.!]?\\s*$",
        Pattern.CASE_INSENSITIVE
    );

    // Follow-up map intent after a conversational list, e.g.:
    // "show me these locations", "show these on map", "put them on the map"
    private static final Pattern MAP_FOLLOWUP_INTENT_PATTERN = Pattern.compile(
        "\\b(show|display|put|add|mark|pin)\\b.*\\b(map|locations|these|them)\\b|\\bshow\\s+me\\s+these\\s+locations\\b",
        Pattern.CASE_INSENSITIVE
    );

    public EnhancedChatCallService(AgentFactory agentFactory, 
                                  List<String> aiFunctionDescriptions,
                                  @Qualifier("templatePrompts") Map<String, String> promptsList,
                                  MessageService messageService,
                                  AIResponseValidationService validationService,
                                  ResponseCacheService cacheService,
                                  AutoLocationExtractionFunction autoLocationExtractionFunction,
                                  GeoapifyService geoapifyService,
                                  ShowLocationFunction showLocationFunction) {
        super(agentFactory, aiFunctionDescriptions, promptsList);
        this.messageService = messageService;
        this.validationService = validationService;
        this.cacheService = cacheService;
        this.autoLocationExtractionFunction = autoLocationExtractionFunction;
        this.geoapifyService = geoapifyService;
        this.showLocationFunction = showLocationFunction;
        log.info("✅ EnhancedChatCallService initialized with validation and caching support");
    }

    /**
     * Processes a chat message using Spring AI's automatic message persistence.
     * Spring AI's chat memory system handles message saving automatically.
     *
     * @param aiMessage The user AI message DTO
     * @return The AI response as a string
     */
    public String processCallMessage(AIMessageDTO aiMessage) {
        log.info("🔵 Processing call message: {}; sessionId: {}", aiMessage.getMessage(), aiMessage.getSessionId());

        try {
            // Check cache first for performance optimization
            if (cacheService.shouldUseCache(aiMessage.getMessage())) {
                String cachedResponse = cacheService.getCachedResponse(aiMessage.getMessage());
                if (cachedResponse != null) {
                    log.info("🚀 Returning cached response for performance boost");
                    return cachedResponse;
                }
            }

            // Process with agent with performance monitoring
            long startTime = System.currentTimeMillis();
            Agent agent = getChatAgent(aiMessage);
            log.info("🤖 About to call agent.processCallMessage()");
            
            String aiResponse = (String) agent.processCallMessage(aiMessage);
            long processingTime = System.currentTimeMillis() - startTime;
            
            // Log performance metrics
            log.info("🤖 Agent response: length={}, time={}ms, performance={}", 
                aiResponse != null ? aiResponse.length() : 0, 
                processingTime,
                processingTime < 5000 ? "EXCELLENT" : processingTime < 10000 ? "GOOD" : "NEEDS OPTIMIZATION");
            
            // Performance warning for slow responses
            if (processingTime > 15000) {
                log.warn("⚠️ Slow response detected: {}ms for message: {}", 
                    processingTime, aiMessage.getMessage().substring(0, Math.min(50, aiMessage.getMessage().length())));
            }

            // Cache the response if it's cacheable
            if (aiResponse != null && cacheService.shouldUseCache(aiMessage.getMessage())) {
                cacheService.cacheResponse(aiMessage.getMessage(), aiResponse);
            }

            log.info("✅ Successfully processed message for session: {}", aiMessage.getSessionId());
            return aiResponse;

        } catch (Exception e) {
            log.error("❌ Exception occurred while processing the message: {}", e.getMessage(), e);
            throw new RuntimeException("Error processing chat message", e);
        }
    }

    /**
     * Processes an AI message and returns the AI response using Spring AI's automatic persistence.
     * Includes JSON schema validation for AI outputs to ensure quality and consistency.
     *
     * @param aiMessage The user AI message DTO  
     * @param returnClassType The expected return type
     * @return The AI response of the specified type
     */
    public <T> T processEntityMessage(AIMessageDTO aiMessage, Class<T> returnClassType) {
        // Add null check for aiMessage
        if (aiMessage == null) {
            log.error("❌ AIMessageDTO is null - cannot process message");
            throw new IllegalArgumentException("AIMessageDTO cannot be null");
        }
        
        if (aiMessage.getMessage() == null || aiMessage.getMessage().trim().isEmpty()) {
            log.error("❌ Message content is null or empty - cannot process");
            throw new IllegalArgumentException("Message content cannot be null or empty");
        }
        
        log.info("🔵 Processing entity message with suggestions: {}; sessionId: {}", aiMessage.getMessage(), aiMessage.getSessionId());

        try {
            // Process with agent - Spring AI chat memory handles message persistence automatically
            Agent agent = getChatAgent(aiMessage);
            T aiResponse = agent.processEntityMessage(aiMessage, returnClassType);

            // Validate AI response if it's an AIMessageResponse
            if (aiResponse instanceof com.springboot.FlomadAIplanner.ai.dto.AIMessageResponse) {
                AIMessageResponse messageResponse = (AIMessageResponse) aiResponse;
                
                // Ensure suggestions are always provided - add defaults if missing
                if (messageResponse.getSuggestions() == null || messageResponse.getSuggestions().isEmpty()) {
                    log.warn("⚠️ AI response missing suggestions - adding default suggestions for session: {}", aiMessage.getSessionId());
                    messageResponse.setSuggestions(java.util.Arrays.asList(
                        "Tell me more about this destination",
                        "What are the best times to visit?",
                        "Help me plan activities for this trip",
                        "What's the estimated budget?"
                    ));
                }
                
                log.info("🔍 Validating AI response schema for session: {}", aiMessage.getSessionId());
                ValidationResult validationResult = validationService.validateWithContext(
                    messageResponse,
                    aiMessage.getSessionId(),
                    "AI Chat Response"
                );
                
                if (!validationResult.isValid()) {
                    log.warn("⚠️ AI response validation failed for session {}: {}", 
                        aiMessage.getSessionId(), validationResult.getErrorMessage());
                    // Log validation issues but don't fail the request to maintain user experience
                    // In production, consider implementing fallback responses or retry logic
                }
                
                // 🚀 AUTOMATIC LOCATION EXTRACTION FOR TOUR/ITINERARY REQUESTS
                // If user message contains tour/itinerary keywords, automatically call location extraction
                if (shouldTriggerLocationExtraction(aiMessage.getMessage())) {
                    triggerAutomaticLocationExtraction(messageResponse.getMessage(), aiMessage.getSessionId());
                }

                // If user asks to "show these locations on map", extract locations from recent assistant context.
                // This handles conversational flows where assistant listed destinations first,
                // then user requests mapping in a follow-up message.
                if (shouldTriggerMapFollowupExtraction(aiMessage.getMessage())) {
                    triggerMapFollowupFromConversationContext(aiMessage.getSessionId(), messageResponse.getMessage());
                }

            // Deterministic fallback for single-location spotlight prompts.
            // If the AI didn't call showLocationFunction/addLocationFunction and the map remains empty,
            // we geocode the requested destination and directly spotlight it.
            try {
                triggerSingleLocationSpotlightFallback(
                    aiMessage.getMessage(),
                    aiMessage.getSessionId()
                );
            } catch (Exception fallbackErr) {
                log.warn("⚠️ Single-location fallback failed (non-fatal): {}", fallbackErr.getMessage());
            }
            }

            log.info("✅ Successfully processed entity message for session: {}", aiMessage.getSessionId());
            return aiResponse;

        } catch (UnsupportedOperationException e) {
            log.warn("Entity message processing is not supported by the selected agent: {}", e.getMessage());
            throw new RuntimeException("This agent does not support entity message processing.", e);
            
        } catch (Exception e) {
            log.error("❌ Error occurred while processing entity message: {}", e.getMessage(), e);
            throw new RuntimeException("Error processing entity-based chat message", e);
        }
    }

    /**
     * Get message count for debugging
     */
    public int getMessageCountForSession(String sessionId) {
        try {
            return messageService.getMessagesBySessionId(sessionId).size();
        } catch (Exception e) {
            log.error("❌ Error getting message count for session {}: {}", sessionId, e.getMessage());
            return 0;
        }
    }
    
    /**
     * Determines if the user message should trigger automatic location extraction.
     * Detects tour/itinerary/travel planning keywords.
     */
    private boolean shouldTriggerLocationExtraction(String message) {
        if (message == null || message.trim().isEmpty()) {
            return false;
        }
        
        boolean matches = TOUR_PATTERN.matcher(message).find();
        if (matches) {
            log.info("🎯 Detected tour/itinerary request - will trigger automatic location extraction");
        }
        return matches;
    }
    
    /**
     * Automatically triggers the location extraction function after AI generates a response.
     * This bypasses the need for GPT to call the function - we do it programmatically.
     */
    private void triggerAutomaticLocationExtraction(String aiResponseMessage, String sessionId) {
        try {
            log.info("🗺️ AUTOMATIC TRIGGER: Calling autoLocationExtractionFunction for session: {}", sessionId);
            log.info("🗺️ Extracting locations from AI response (length: {} chars)", 
                aiResponseMessage != null ? aiResponseMessage.length() : 0);
            
            // Create the request object for the function
            AutoLocationExtractionFunction.LocationExtractionRequest request = 
                new AutoLocationExtractionFunction.LocationExtractionRequest();
            request.setMessage(aiResponseMessage);
            request.setSessionId(sessionId);
            
            // Call the function directly
            String extractionResult = autoLocationExtractionFunction.apply(request);
            
            log.info("✅ Location extraction completed: {}", extractionResult);
            
        } catch (Exception e) {
            log.error("❌ Error during automatic location extraction: {}", e.getMessage(), e);
            // Don't fail the entire request - location extraction is supplementary
        }
    }

    private boolean shouldTriggerMapFollowupExtraction(String userMessage) {
        if (userMessage == null || userMessage.isBlank()) return false;
        return MAP_FOLLOWUP_INTENT_PATTERN.matcher(userMessage).find();
    }

    private void triggerMapFollowupFromConversationContext(String sessionId, String currentAssistantResponse) {
        try {
            String extractionSource = getLatestAssistantListMessage(sessionId, currentAssistantResponse);
            if (extractionSource == null || extractionSource.isBlank()) {
                log.info("🗺️ Map follow-up detected but no suitable assistant list message found in context.");
                return;
            }

            log.info("🗺️ Map follow-up trigger: extracting locations from prior assistant response for session={}", sessionId);
            triggerAutomaticLocationExtraction(extractionSource, sessionId);
        } catch (Exception e) {
            log.warn("⚠️ Map follow-up extraction failed (non-fatal): {}", e.getMessage());
        }
    }

    private String getLatestAssistantListMessage(String sessionId, String currentAssistantResponse) {
        List<Message> messages = messageService.getMessagesBySessionIdWithLimit(sessionId, 40);
        if (messages == null || messages.isEmpty()) return null;

        // Scan newest -> oldest
        for (int i = messages.size() - 1; i >= 0; i--) {
            Message message = messages.get(i);
            if (message.getMessageType() != MessageType.ASSISTANT) continue;
            String content = message.getContent();
            if (content == null || content.isBlank()) continue;

            // Skip the current AI response that may be a generic "no locations marked" message.
            if (currentAssistantResponse != null && content.trim().equals(currentAssistantResponse.trim())) continue;

            String lower = content.toLowerCase();
            if (lower.contains("there are currently no locations marked on your map")) continue;
            if (lower.contains("no locations marked on your map")) continue;

            // Prefer richer destination-list style responses to extract place names from.
            boolean likelyLocationList = content.contains("•")
                    || content.contains("\n-")
                    || lower.contains("must-visit")
                    || lower.contains("here are")
                    || lower.contains("places");
            if (likelyLocationList && content.length() > 80) {
                return content;
            }
        }

        return null;
    }

    private void triggerSingleLocationSpotlightFallback(String userMessage, String sessionId) {
        if (sessionId == null || sessionId.isBlank()) return;
        if (userMessage == null || userMessage.isBlank()) return;

        // Don't interfere with multi-city/tour logic.
        if (shouldTriggerLocationExtraction(userMessage)) return;

        // Only handle very simple spotlight prompts.
        var match = SIMPLE_SPOTLIGHT_PATTERN.matcher(userMessage);
        if (!match.find()) return;

        // Avoid “two cities in one sentence” ambiguity.
        String rawDestination = match.group(2);
        if (rawDestination == null) return;
        String destination = rawDestination.trim();
        if (destination.isEmpty()) return;
        if (destination.contains(",") || destination.toLowerCase().contains(" and ")) return;

        log.info("🛟 Fallback spotlight: geocoding destination='{}' for session='{}'", destination, sessionId);
        JsonNode geocode = geoapifyService.geocode(destination);

        if (geocode == null || geocode.has("error")) {
            log.warn("🛟 Fallback spotlight: geocode failed: {}", geocode != null ? geocode.toString() : "null");
            return;
        }

        JsonNode results = geocode.get("results");
        if (results == null || !results.isArray() || results.isEmpty()) return;

        JsonNode first = results.get(0);
        double lat = first.path("lat").asDouble(Double.NaN);
        double lon = first.path("lon").asDouble(Double.NaN);
        if (Double.isNaN(lat) || Double.isNaN(lon)) return;

        String name = first.hasNonNull("name") ? first.get("name").asText() : destination;

        ShowLocationFunction.ShowLocationRequest request = new ShowLocationFunction.ShowLocationRequest();
        request.setLatitude(lat);
        request.setLongitude(lon);
        request.setName(name);
        request.setType("attraction");
        request.setSessionId(sessionId);

        // Reuse the existing spotlight logic to keep behavior consistent:
        // clear locations, save new one, and broadcast via websocket.
        showLocationFunction.apply(request);
    }
}