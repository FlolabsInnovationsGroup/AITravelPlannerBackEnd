package com.springboot.FlomadAIplanner.ai.dto;

import lombok.Builder;
import lombok.Data;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;

import java.util.List;

@Data
@Builder
public class MessageProcessingStrategyDTO {
    private ChatClient client;
    private AIMessageDTO aiMessage;
    private List<String> requiredFunctions;
    private Prompt systemPrompt;
    private Class<?> returnClassType; // Added for entity return processing

    // No need for explicit getters or setters as @Data generates them
}

