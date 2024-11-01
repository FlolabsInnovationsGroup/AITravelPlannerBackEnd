package com.springboot.FlomadAIplanner.util

import groovy.util.logging.Slf4j
import org.springframework.ai.chat.client.ChatClient
import org.springframework.boot.test.context.TestComponent

/**
 * Utility class for evaluating the relevance of a response to a given query.
 * This class is intended for use in tests and resides in the test source code.
 */
@Slf4j
@TestComponent
class ResponseEvaluator {

    private static final String EVALUATION_PROMPT_TEMPLATE = """
            Your task is to evaluate if the response for the query
            is in line with the context information provided.
            
            You have two options to answer: YES or NO.
            
            Answer YES if the response for the query
            is in line with the context information otherwise, answer NO.
            
            Query:
            {query}
            
            Response:
            {response}
            
            Answer:
            """

    private final ChatClient chatClient

    /**
     * Constructs a ResponseEvaluator with the given ChatClient.
     *
     * @param clientBuilder The ChatClient to use for communication with the LLM.
     */
    ResponseEvaluator(ChatClient.Builder clientBuilder) {
        log.info("Creating a ResponseEvaluator with the provided ChatClient")
        this.chatClient = clientBuilder.build()
    }

    /**
     * Evaluates whether the response is relevant to the query.
     *
     * @param query The user's query.
     * @param response The response to evaluate.
     * @return {@code true} if the response is relevant {@code false} otherwise.
     */
    boolean isResponseRelevant(String query, String response) {
        try {
            String evaluationResult = chatClient.prompt()
                    .user(u -> u.text(EVALUATION_PROMPT_TEMPLATE)
                            .param("query", query)
                            .param("response", response))
                    .call()
                    .content()
                    .trim()
                    .toUpperCase()

            return "YES".equals(evaluationResult)
        } catch (Exception e) {
            // Log the exception if necessary and decide how to handle it
            // For now, we'll assume the response is not relevant if an error occurs
            log.error("Error occurred while evaluating the response", e)
            return false
        }
    }
}
