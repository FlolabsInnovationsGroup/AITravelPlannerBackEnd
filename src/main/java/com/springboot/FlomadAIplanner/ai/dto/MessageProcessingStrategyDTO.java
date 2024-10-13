package com.springboot.FlomadAIplanner.ai.dto;

import com.springboot.FlomadAIplanner.ai.agents.Agent;
import lombok.Builder;
import lombok.Data;
import org.springframework.ai.chat.prompt.Prompt;

import java.util.List;

@Data
@Builder
public class MessageProcessingStrategyDTO<T> {
    private Agent agent;
    private AIMessageDTO aiMessage;
    private List<String> requiredFunctions;
    private Prompt systemPrompt;
    private Class<T> returnClassType; // Added for entity return processing

    // No need for explicit getters or setters as @Data generates them
}

