package com.springboot.FlomadAIplanner.ai.agents;

import com.springboot.FlomadAIplanner.ai.dto.AIMessageDTO;

/**
 * Marks Agent as capable of processing entity messages.
 */
public interface EntityReturnMessageProcessing {
    /**
     * Processes the user message and returns the entity of required type.
     *
     * @param aiMessage  the message to process
     * @param returnType the return type
     * @param <T>        the return type
     * @return the entity
     */
    <T> T processEntityMessage(AIMessageDTO aiMessage, Class<T> returnType);
}
