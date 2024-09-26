package com.springboot.FlomadAIplanner.ai.agents;

import com.springboot.FlomadAIplanner.ai.dto.AIMessageDTO;

/**
 * Marks Agent as capable of processing stream messages.
 */
public interface StreamMessageProcessing<S> {
    /**
     * Processes the user message and returns the stream Flux object required type.
     *
     * @param aiMessage the message to process
     * @return the stream
     */
    S processStreamMessage(AIMessageDTO aiMessage, String sessionID);
}
