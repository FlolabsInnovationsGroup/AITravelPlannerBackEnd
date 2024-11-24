package com.springboot.FlomadAIplanner.ai.agents;

import com.springboot.FlomadAIplanner.ai.dto.AIMessageDTO;

import java.util.List;


/**
 * Represents an AI Agent responsible for processing different types of messages.
 * This interface defines the contract for handling various AI message processing
 * operations such as call messages, stream messages, and entity-based messages.
 */
public interface Agent {

    /**
     * Process a call message and return the response.
     * This method is intended to handle standard synchronous message processing
     * where the user sends a message, and the agent provides an immediate response.
     *
     * @param aiMessage The message to process.
     * @return The processed message response.
     */
    Object processCallMessage(AIMessageDTO aiMessage);

    /**
     * Process a stream message and return the response as a Flux.
     * This method is designed for live response generation, where the output
     * is streamed back to the client in real time.
     *
     * @param aiMessage The message to process.
     * @return The processed message response as Flux<String>.
     */
    Object processStreamMessage(AIMessageDTO aiMessage);

    /**
     * Determines if this agent supports the provided message.
     * This method is used to check if the agent is capable of handling
     * the given message based on specific criteria (e.g., message type, context).
     *
     * @param inputMessage The input message to check.
     * @return true if the message is supported, false otherwise.
     */
    boolean supports(AIMessageDTO inputMessage);

    /**
     * Process an entity-based message and return a typed response.
     * This method allows for processing messages that require structured
     * and context-aware responses, such as providing suggestions, recommendations,
     * or any complex entity-based data. The response type is determined
     * by the `returnClassType` parameter.
     *
     * @param aiMessage The message to process.
     * @param returnClassType The class type of the response.
     * @param <T> The type of the response object.
     * @return The processed entity message response of type <T>.
     */
    <T> T processEntityMessage(AIMessageDTO aiMessage, Class<T> returnClassType);

    /**
     * Generate context-aware suggestions for the next user input.
     * This method dynamically generates suggestions based on the provided context.
     * Suggestions are designed to guide the user and keep the conversation engaging.
     *
     * @param context The input context or the main response text.
     * @return A list of suggested next inputs or actions.
     */
    default List<String> generateSuggestions(String context) {
        throw new UnsupportedOperationException("This agent does not support suggestion generation.");
    }

    /**
     * Provides a brief description of the agent and its primary use case.
     * This can be used for logging or debugging to identify the agent's purpose.
     *
     * @return A string describing the agent.
     */
    default String getDescription() {
        return "Generic AI Agent";
    }
}

