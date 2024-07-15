package com.springboot.FlomadAIplanner.ai.agents;

/**
 * Interface that combines call, entity, and stream message processing capabilities.
 */
public interface TextAgent<C, S> extends CallMessageProcessing<C>, EntityReturnMessageProcessing, StreamMessageProcessing<S> {
    // This interface inherits all methods from the extended interfaces
}