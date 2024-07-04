package com.springboot.FlomadAIplanner.ai.agents.messageProcessing;

import com.springboot.FlomadAIplanner.ai.agents.Agent;
import com.springboot.FlomadAIplanner.ai.dto.AIMessageDTO;

public interface MessageProcessingStrategy<T> {
    T processMessage(AIMessageDTO message, Agent agent, String prompt);
}
