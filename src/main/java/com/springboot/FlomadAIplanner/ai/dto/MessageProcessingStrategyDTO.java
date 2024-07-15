package com.springboot.FlomadAIplanner.ai.dto;

import com.springboot.FlomadAIplanner.ai.agents.TextTextAgentImpl;
import lombok.Builder;
import lombok.Data;
import org.springframework.ai.chat.prompt.Prompt;

import java.util.List;

@Data
@Builder
public class MessageProcessingStrategyDTO {
    private AIMessageDTO aiMessage;
    private TextTextAgentImpl<?, ?> agent;
    private List<String> requiredFunctions;
    private Prompt systemPrompt;
    private Class<?> returnClassType;
}
