package com.springboot.FlomadAIplanner.ai.service;

import com.springboot.FlomadAIplanner.ai.dto.AIMessageDTO;
import com.springboot.FlomadAIplanner.ai.factory.ChatClientFactory;
import com.springboot.FlomadAIplanner.ai.service.ChatClientSuggestedProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * ChatCallService extends AbstractChatService to provide specific functionality for processing chat messages.
 * This service analyzes the message content to determine required functions and processes the message accordingly.
 */
@Slf4j
@Service
public class ChatCallService extends AbstractChatService {

    private final Map<String, ChatClientSuggestedProperties> keywordToFunctionMap;

    /**
     * Constructor for ChatCallService.
     * 
     * @param chatClientFactory Factory for creating chat clients
     * @param aiFunctionDescriptions List of available AI function descriptions
     */
    public ChatCallService(ChatClientFactory chatClientFactory, List<String> aiFunctionDescriptions, Map<String, String> promptsList) {
        super(chatClientFactory, aiFunctionDescriptions, promptsList);
        log.info("ChatCallService initialized with AI function descriptions: {}", aiFunctionDescriptions);
    
        keywordToFunctionMap = new HashMap<>();
        initializeKeywordToFunctionMap();
    }

    /**
     * Processes a message and determines the appropriate response.
     * 
     * @param message The user message DTO
     * @return The response content
     */
    public String processMessage(AIMessageDTO message) {
        log.info("Processing message: {}", message.getMessage());
        ChatClientSuggestedProperties properties = getChatClientPropertiesSuggestion(message.getMessage());
    
        if (properties.getFunctionNames().isEmpty()) {
            log.info("No required functions found, asking question without functions.");
            return askQuestion(message);
        } else {
            log.info("Required functions found, asking question with functions: {}", properties.getFunctionNames());
            return askQuestionWithFunctions(properties.getSystemPrompt(), message, properties.getFunctionNames());
        }
    }    

    /**
     * Asks a question to the chat client without any additional functions.
     * 
     * @param message The user message DTO
     * @return The response content
     */
    private String askQuestion(AIMessageDTO message) {
        log.debug("Asking question without functions for message: {}", message.getMessage());
        return getChatClient().prompt()
                .system(promptsList.get("SingleQuestionPrompt.txt"))
                .user(message.getMessage())
                .call()
                .content();
    }

    /**
     * Asks a question to the chat client with additional functions.
     * 
     * @param systemPrompt The system prompt
     * @param message The user message DTO
     * @param functions List of required functions
     * @return The response content
     */
    private String askQuestionWithFunctions(String systemPrompt, AIMessageDTO message, List<String> functionNames) {
        log.debug("Asking question with functions for message: {}, functions: {}", message.getMessage(), functionNames);
        return getChatClient().prompt()
                .system(systemPrompt)
                .user(message.getMessage())
                .functions(functionNames.toArray(new String[0]))
                .call()
                .content();
    }

    /**
     * Initializes the keyword to function map with predefined functions.
     */
    private void initializeKeywordToFunctionMap() {
        keywordToFunctionMap.put("history currency", new ChatClientSuggestedProperties(List.of("HistoryCurrencyFunction"), "Retrieve historical currency data"));
        keywordToFunctionMap.put("latest currency", new ChatClientSuggestedProperties(List.of("LatestCurrencyFunction"), "Retrieve latest currency data"));
        keywordToFunctionMap.put("support currency", new ChatClientSuggestedProperties(List.of("SupportCurrencyFunction"), "Support currency operations"));
        keywordToFunctionMap.put("ai function", new ChatClientSuggestedProperties(List.of("AIFunction"), "Perform AI operations"));
        keywordToFunctionMap.put("current date time", new ChatClientSuggestedProperties(List.of("CurrentDateTimeFunction"), "Get current date and time"));
        keywordToFunctionMap.put("distance calculator", new ChatClientSuggestedProperties(List.of("DistanceCalculatorFunction"), "Calculate distance between locations"));
        keywordToFunctionMap.put("flight search", new ChatClientSuggestedProperties(List.of("FlightSearchMockFunction"), "Search for flights"));
        keywordToFunctionMap.put("hotel offers search", new ChatClientSuggestedProperties(List.of("HotelOffersSearchFunction"), "Search for hotel offers"));
        keywordToFunctionMap.put("hotel search location", new ChatClientSuggestedProperties(List.of("HotelSearchByLocationFunction"), "Search for hotels by location"));
        keywordToFunctionMap.put("place suggestion", new ChatClientSuggestedProperties(List.of("PlaceSuggestionFunction"), "Suggest places to visit"));
        keywordToFunctionMap.put("place text search", new ChatClientSuggestedProperties(List.of("PlaceTextSearchFunction"), "Search for places by text"));
        keywordToFunctionMap.put("tours free text search", new ChatClientSuggestedProperties(List.of("ToursFreeTextSearchFunction"), "Search for tours by free text"));
        keywordToFunctionMap.put("weather forecast", new ChatClientSuggestedProperties(List.of("WeatherForecastFunctionMock"), "Get weather forecast"));
    }

    /**
     * Suggests properties for the chat client based on the message content.
     * 
     * @param message The user message
     * @return Suggested properties for the chat client
     */
    private ChatClientSuggestedProperties getChatClientPropertiesSuggestion(String message) {
        List<String> requiredFunctions = new ArrayList<>();
        String systemPrompt = "systemPrompt.";

    //Analyze the message to determine required functions

        for (Map.Entry<String, ChatClientSuggestedProperties> entry : keywordToFunctionMap.entrySet()) {
            if (message.toLowerCase().contains(entry.getKey())) {
                requiredFunctions.addAll(entry.getValue().getFunctionNames());
            }
        }

        // Determine system prompt based on message content
        if (message.toLowerCase().contains("help")) {
            systemPrompt = "You have asked for help. How can I assist you?";
        } else if (!requiredFunctions.isEmpty()) {
            systemPrompt = "You have requested information on: " + requiredFunctions.toString();
        } else {
            systemPrompt = "I'm here to assist you with your queries.";
        }

        ChatClientSuggestedProperties properties = new ChatClientSuggestedProperties();
        properties.setFunctionNames(requiredFunctions);
        properties.setSystemPrompt(systemPrompt);

        return properties;
    }
}