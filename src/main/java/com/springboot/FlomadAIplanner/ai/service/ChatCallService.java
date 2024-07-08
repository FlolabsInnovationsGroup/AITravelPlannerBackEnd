package com.springboot.FlomadAIplanner.ai.service;

import com.springboot.FlomadAIplanner.ai.dto.AIMessageDTO;
import com.springboot.FlomadAIplanner.ai.factory.ChatClientFactory;
import com.springboot.FlomadAIplanner.dto.ChatClientSuggestedProperties;
import com.springboot.FlomadAIplanner.ai.service.FunctionNameSuggestion;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Slf4j
@Service
public class ChatCallService extends AbstractChatService {

    private final Map<String, FunctionNameSuggestion> keywordToFunctionMap;

    public ChatCallService(ChatClientFactory chatClientFactory, List<String> aiFunctionDescriptions) {
        super(chatClientFactory, aiFunctionDescriptions);
        log.info("ChatCallService initialized with AI function descriptions: {}", aiFunctionDescriptions);
    
        keywordToFunctionMap = new HashMap<>();
        initializeKeywordToFunctionMap();
    }

    public String processMessage(AIMessageDTO message) {
        log.info("Processing message: {}", message.getMessage());
        ChatClientSuggestedProperties properties = getChatClientPropertiesSuggestion(message.getMessage());
    
        if (properties.getRequiredFunctions().isEmpty()) {
            log.info("No required functions found, asking question without functions.");
            return askQuestion(message);
        } else {
            log.info("Required functions found, asking question with functions: {}", properties.getRequiredFunctions());
            return askQuestionWithFunctions(properties.getSystemPrompt(), message, properties.getRequiredFunctions());
        }
    }    

    private String askQuestion(AIMessageDTO message) {
        log.debug("Asking question without functions for message: {}", message.getMessage());
        return getChatClient().prompt()
                .system(singleQuestionPrompt)
                .user(message.getMessage())
                .call()
                .content();
    }

    private String askQuestionWithFunctions(String systemPrompt, AIMessageDTO message, List<FunctionNameSuggestion> functions) {
        log.debug("Asking question with functions for message: {}, functions: {}", message.getMessage(), functions);
        return getChatClient().prompt()
                .system(systemPrompt)
                .user(message.getMessage())
                .functions(functionNames(functions))
                .call()
                .content();
    }

    // Initialize the keyword to function map with your functions
    private void initializeKeywordToFunctionMap() {
        keywordToFunctionMap.put("history currency", new FunctionNameSuggestion("HistoryCurrencyFunction", "Retrieve historical currency data"));
        keywordToFunctionMap.put("latest currency", new FunctionNameSuggestion("LatestCurrencyFunction",  "Retrieve latest currency data"));
        keywordToFunctionMap.put("support currency", new FunctionNameSuggestion("SupportCurrencyFunction", "Support currency operations"));
        keywordToFunctionMap.put("ai function", new FunctionNameSuggestion("AIFunction", "Perform AI operations"));
        keywordToFunctionMap.put("current date time", new FunctionNameSuggestion("CurrentDateTimeFunction", "Get current date and time"));
        keywordToFunctionMap.put("distance calculator", new FunctionNameSuggestion("DistanceCalculatorFunction", "Calculate distance between locations"));
        keywordToFunctionMap.put("flight search", new FunctionNameSuggestion("FlightSearchMockFunction", "Search for flights"));
        keywordToFunctionMap.put("hotel offers search", new FunctionNameSuggestion("HotelOffersSearchFunction", "Search for hotel offers"));
        keywordToFunctionMap.put("hotel search location", new FunctionNameSuggestion("HotelSearchByLocationFunction", "Search for hotels by location"));
        keywordToFunctionMap.put("place suggestion", new FunctionNameSuggestion("PlaceSuggestionFunction","Suggest places to visit"));
        keywordToFunctionMap.put("place text search", new FunctionNameSuggestion("PlaceTextSearchFunction", "Search for places by text"));
        keywordToFunctionMap.put("tours free text search", new FunctionNameSuggestion("ToursFreeTextSearchFunction", "Search for tours by free text"));
        keywordToFunctionMap.put("weather forecast", new FunctionNameSuggestion("WeatherForecastFunctionMock", "Get weather forecast"));
    }

    private ChatClientSuggestedProperties getChatClientPropertiesSuggestion(String message) {
        List<FunctionNameSuggestion> requiredFunctions = new ArrayList<>();
        String systemPrompt = "systemPrompt.";

        // Analyze the message to determine required functions
        for (Map.Entry<String, FunctionNameSuggestion> entry : keywordToFunctionMap.entrySet()) {
            if (message.toLowerCase().contains(entry.getKey())) {
                requiredFunctions.add(entry.getValue());
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

        return new ChatClientSuggestedProperties(systemPrompt, requiredFunctions);
    }
}