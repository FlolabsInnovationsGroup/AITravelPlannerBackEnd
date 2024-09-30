package com.springboot.FlomadAIplanner.ai.dto;

import com.springboot.FlomadAIplanner.ai.agents.text.TextAgentImpl;
import lombok.Builder;
import lombok.Data;
import org.springframework.ai.chat.prompt.Prompt;

import java.util.List;

@Data
@Builder
public class MessageProcessingStrategyDTO {
    private AIMessageDTO aiMessage;
    private TextAgentImpl<?, ?> agent;
    private List<String> requiredFunctions;
    private Prompt systemPrompt;
    private Class<?> returnClassType;

    private String sessionID;
}
