package com.springboot.FlomadAIplanner.ai.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.List;
import java.util.HashMap;
import java.util.ArrayList;

/**
 * Service for counting and managing tokens to prevent exceeding OpenAI's context limits.
 * Uses a simplified estimation approach based on character counting.
 */
@Service
@Slf4j
public class TokenCountingService {

    // OpenAI's gpt-3.5-turbo has a 16,385 token limit
    private static final int MAX_TOKENS = 16385;
    // Reserve tokens for response and safety margin
    private static final int SAFETY_MARGIN = 1000;
    private static final int MAX_INPUT_TOKENS = MAX_TOKENS - SAFETY_MARGIN;
    
    // Approximate characters per token (rough estimation)
    private static final int CHARS_PER_TOKEN = 4;

    /**
     * Estimates token count for a given text using character-based approximation.
     * This is a simplified approach - for production, consider using tiktoken or similar.
     * 
     * @param text The text to count tokens for
     * @return Estimated token count
     */
    public int estimateTokenCount(String text) {
        if (text == null || text.isEmpty()) {
            return 0;
        }
        return Math.max(1, text.length() / CHARS_PER_TOKEN);
    }

    /**
     * Estimates total token count for a prompt with parameters.
     * 
     * @param basePrompt The base prompt template
     * @param promptsList Map of available prompts
     * @param functionsList List of available functions
     * @param userMessage The user's message
     * @return Estimated total token count
     */
    public int estimateTotalTokenCount(String basePrompt, Map<String, String> promptsList, 
                                     List<String> functionsList, String userMessage) {
        int basePromptTokens = estimateTokenCount(basePrompt);
        int promptsListTokens = estimateTokenCount(promptsList.toString());
        int functionsListTokens = estimateTokenCount(functionsList.toString());
        int userMessageTokens = estimateTokenCount(userMessage);
        
        int totalTokens = basePromptTokens + promptsListTokens + functionsListTokens + userMessageTokens;
        
        log.debug("Token estimation: base={}, prompts={}, functions={}, message={}, total={}", 
                 basePromptTokens, promptsListTokens, functionsListTokens, userMessageTokens, totalTokens);
        
        return totalTokens;
    }

    /**
     * Checks if the total token count would exceed the limit.
     * 
     * @param totalTokens The estimated total token count
     * @return true if within limits, false if exceeds limits
     */
    public boolean isWithinTokenLimit(int totalTokens) {
        return totalTokens <= MAX_INPUT_TOKENS;
    }

    /**
     * Truncates the prompts list to fit within token limits while preserving critical prompts.
     * 
     * @param promptsList Original prompts map
     * @param remainingTokens Tokens available for prompts
     * @return Truncated prompts map
     */
    public Map<String, String> truncatePromptsList(Map<String, String> promptsList, int remainingTokens) {
        Map<String, String> truncatedPrompts = new HashMap<>();
        
        // Critical prompts that should always be included
        String[] criticalPrompts = {
            "EmergencyAssistancePrompt.txt",
            "BudgetOptimizationPrompt.txt", 
            "LocationRequestPrompt.txt",
            "BookingConfirmationPrompt.txt",
            "SingleQuestionPrompt.txt"
        };
        
        int usedTokens = 0;
        
        // First, include critical prompts
        for (String criticalPrompt : criticalPrompts) {
            if (promptsList.containsKey(criticalPrompt)) {
                String promptContent = promptsList.get(criticalPrompt);
                int promptTokens = estimateTokenCount(promptContent);
                if (usedTokens + promptTokens <= remainingTokens) {
                    truncatedPrompts.put(criticalPrompt, promptContent);
                    usedTokens += promptTokens;
                } else {
                    // Truncate this prompt content if it's too long
                    String truncatedContent = truncateText(promptContent, remainingTokens - usedTokens);
                    if (!truncatedContent.isEmpty()) {
                        truncatedPrompts.put(criticalPrompt, truncatedContent);
                        usedTokens += estimateTokenCount(truncatedContent);
                    }
                    break;
                }
            }
        }
        
        // Then include other prompts if space allows
        for (Map.Entry<String, String> entry : promptsList.entrySet()) {
            if (!truncatedPrompts.containsKey(entry.getKey())) {
                int promptTokens = estimateTokenCount(entry.getValue());
                if (usedTokens + promptTokens <= remainingTokens) {
                    truncatedPrompts.put(entry.getKey(), entry.getValue());
                    usedTokens += promptTokens;
                } else {
                    break;
                }
            }
        }
        
        log.info("Truncated prompts list from {} to {} prompts, using {} tokens", 
                promptsList.size(), truncatedPrompts.size(), usedTokens);
        
        return truncatedPrompts;
    }

    /**
     * Truncates the functions list to fit within token limits.
     * 
     * @param functionsList Original functions list
     * @param remainingTokens Tokens available for functions
     * @return Truncated functions list
     */
    public List<String> truncateFunctionsList(List<String> functionsList, int remainingTokens) {
        List<String> truncatedFunctions = new ArrayList<>();
        
        // Critical functions that should always be included
        String[] criticalFunctions = {
            "showLocationFunction",
            "addLocationFunction", 
            "searchFlightsFunction",
            "searchHotelsFunction",
            "getWeatherFunction"
        };
        
        int usedTokens = 0;
        
        // First, include critical functions
        for (String criticalFunction : criticalFunctions) {
            if (functionsList.contains(criticalFunction)) {
                int functionTokens = estimateTokenCount(criticalFunction);
                if (usedTokens + functionTokens <= remainingTokens) {
                    truncatedFunctions.add(criticalFunction);
                    usedTokens += functionTokens;
                }
            }
        }
        
        // Then include other functions if space allows
        for (String function : functionsList) {
            if (!truncatedFunctions.contains(function)) {
                int functionTokens = estimateTokenCount(function);
                if (usedTokens + functionTokens <= remainingTokens) {
                    truncatedFunctions.add(function);
                    usedTokens += functionTokens;
                } else {
                    break;
                }
            }
        }
        
        log.info("Truncated functions list from {} to {} functions, using {} tokens", 
                functionsList.size(), truncatedFunctions.size(), usedTokens);
        
        return truncatedFunctions;
    }

    /**
     * Truncates text to fit within a specific token limit.
     * 
     * @param text The text to truncate
     * @param maxTokens Maximum tokens allowed
     * @return Truncated text
     */
    public String truncateText(String text, int maxTokens) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        
        int estimatedTokens = estimateTokenCount(text);
        if (estimatedTokens <= maxTokens) {
            return text;
        }
        
        // Calculate approximate character limit
        int maxChars = maxTokens * CHARS_PER_TOKEN;
        if (text.length() <= maxChars) {
            return text;
        }
        
        // Truncate and add indicator
        String truncated = text.substring(0, maxChars - 20) + "...[truncated]";
        return truncated;
    }

    /**
     * Gets the maximum allowed input tokens.
     * 
     * @return Maximum input tokens
     */
    public int getMaxInputTokens() {
        return MAX_INPUT_TOKENS;
    }
}