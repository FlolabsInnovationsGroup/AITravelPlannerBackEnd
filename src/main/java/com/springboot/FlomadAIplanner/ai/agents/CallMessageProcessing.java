package com.springboot.FlomadAIplanner.ai.agents;

import com.springboot.FlomadAIplanner.ai.dto.AIMessageDTO;

/**
 * Marks Agent as capable of processing call messages.
 */
public interface CallMessageProcessing<C> {
    /**
     * Processes the user message and returns the call object of required type.
     *
     * @param aiMessage the message to process
     * @return the call
     */
    C processCallMessage(AIMessageDTO aiMessage);
}
