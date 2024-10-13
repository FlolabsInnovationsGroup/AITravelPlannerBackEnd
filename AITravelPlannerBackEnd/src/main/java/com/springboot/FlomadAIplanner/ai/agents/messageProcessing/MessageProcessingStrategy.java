package com.springboot.FlomadAIplanner.ai.agents.messageProcessing;

import com.springboot.FlomadAIplanner.ai.dto.MessageProcessingStrategyDTO;

public interface MessageProcessingStrategy<T> {
    T processMessage(MessageProcessingStrategyDTO messageProcessingStrategyDTO);
}
