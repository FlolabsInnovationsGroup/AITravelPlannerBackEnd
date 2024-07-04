package com.springboot.FlomadAIplanner.ai.agents.messageProcessing;

import com.springboot.FlomadAIplanner.ai.agents.Agent;
import com.springboot.FlomadAIplanner.ai.dto.AIMessageDTO;
import org.springframework.ai.chat.prompt.PromptTemplate;

public interface MessageProcessingStrategy<T> {
    T processMessage(AIMessageDTO message, Agent<T> agent, PromptTemplate promptTemplate);
}
