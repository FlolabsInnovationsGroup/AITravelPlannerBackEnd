package com.springboot.FlomadAIplanner.ai.agents.messageProcessing;

import com.springboot.FlomadAIplanner.ai.agents.Agent;
import com.springboot.FlomadAIplanner.ai.dto.AIMessageDTO;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.core.io.Resource;

public interface MessageProcessingStrategy<T> {
    T processMessage(Agent agent, Prompt prompt);
}
