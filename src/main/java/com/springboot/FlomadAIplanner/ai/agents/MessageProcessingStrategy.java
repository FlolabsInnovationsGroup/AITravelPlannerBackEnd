package com.springboot.FlomadAIplanner.ai.agents;

import com.springboot.FlomadAIplanner.ai.dto.MessageProcessingStrategyDTO;

public interface MessageProcessingStrategy<T> {
    T processMessage(MessageProcessingStrategyDTO<T> messageProcessingStrategyDTO);
}
