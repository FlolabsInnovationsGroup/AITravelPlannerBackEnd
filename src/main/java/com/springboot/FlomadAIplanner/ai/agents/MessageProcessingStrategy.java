package com.springboot.FlomadAIplanner.ai.agents;

public interface MessageProcessingStrategy<I, O> {
    O processMessage(I messageProcessingStrategyDTO);
}
