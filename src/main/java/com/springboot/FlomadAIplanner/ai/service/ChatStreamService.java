package com.springboot.FlomadAIplanner.ai.service;

import com.springboot.FlomadAIplanner.ai.dto.AIMessageDTO;
import com.springboot.FlomadAIplanner.ai.dto.ChatClientSuggestedProperties;
import com.springboot.FlomadAIplanner.ai.factory.ChatClientFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;

import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY;

/**
 * ChatStreamService processes messages in a streaming manner.
 * It extends AbstractChatService to leverage common chat-related functionalities.
 */
@Slf4j
@Service
public class ChatStreamService extends AbstractChatService {

    /**
     * Constructs a new ChatStreamService.
     *
     * @param chatClientFactory      the factory to create ChatClient instances
     * @param aiFunctionDescriptions the list of AI function descriptions
     */
    public ChatStreamService(ChatClientFactory chatClientFactory, List<String> aiFunctionDescriptions, Map<String, String> promptsList) {
        super(chatClientFactory, aiFunctionDescriptions, promptsList);
        log.info("ChatCallService initialized with AI function descriptions: {}", aiFunctionDescriptions);
    }

    /**
     * Processes a aiMessage and determines the appropriate response.
     *
     * @param aiMessage The user aiMessage DTO
     * @return The response content
     */
    public Flux<String> processMessage(AIMessageDTO aiMessage) {
        log.info("Processing aiMessage: {}", aiMessage.getMessage());
        ChatClientSuggestedProperties properties = getSuggestedChatClientProperties(aiMessage.getMessage());

        return askQuestionWithParameters(properties, aiMessage);
    }

    /**
     * Retrieves the suggested chat client properties based on the aiMessage content.
     *
     * @param clientProperties The chat client suggested properties
     * @param aiMessage        The user aiMessage
     * @return processed response for user from LLM
     */
    private Flux<String> askQuestionWithParameters(ChatClientSuggestedProperties clientProperties, AIMessageDTO aiMessage) {
        List<String> functions = clientProperties.getFunctionNames();
        String systemPrompt = clientProperties.getSystemPrompt();

        log.debug("Asking question with parameters for aiMessage: {}, functions: {}", aiMessage.getMessage(), functions);

        return getChatClient().prompt()
                .system(systemPrompt)
                .user(aiMessage.getMessage())
                .advisors(a -> a.param(CHAT_MEMORY_CONVERSATION_ID_KEY, aiMessage.getSessionId()))
                .functions(functions.toArray(new String[0]))
                .stream()
                .content();
    }

}