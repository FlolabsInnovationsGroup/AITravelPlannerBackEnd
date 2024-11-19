package com.springboot.FlomadAIplanner.ai.agents;

import com.springboot.FlomadAIplanner.ai.dto.AIMessageDTO;

public interface Agent {
    /**
     * Process a call message and return the response.
     *
     * @param aiMessage The message to process.
     * @return The processed message response.
     */
    Object processCallMessage(AIMessageDTO aiMessage);

    /**
     * Process a stream message and return the response as a Flux.
     *
     * @param aiMessage The message to process.
     * @return The processed message response as Flux<String>
     */
    Object processStreamMessage(AIMessageDTO aiMessage);  // New method for stream processing

    /**
     * Determines if this agent supports the provided message.
     *
     * @param inputMessage The input message to check.
     * @return true if supported, false otherwise.
     */
    boolean supports(AIMessageDTO inputMessage);

    /**
     * Process an entity-based message and return a typed response.
     *
     * @param aiMessage The message to process.
     * @param returnClassType The class type of the response.
     * @param <T> The type of the response object.
     * @return The processed entity message response.
     */
    <T> T processEntityMessage(AIMessageDTO aiMessage, Class<T> returnClassType);
}

