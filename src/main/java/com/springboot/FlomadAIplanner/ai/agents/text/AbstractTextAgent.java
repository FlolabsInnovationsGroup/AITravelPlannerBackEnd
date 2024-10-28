package com.springboot.FlomadAIplanner.ai.agents.text;

import com.springboot.FlomadAIplanner.ai.agents.MessageProcessingStrategy;
import com.springboot.FlomadAIplanner.ai.dto.AIMessageDTO;
import com.springboot.FlomadAIplanner.ai.dto.ChatClientSuggestedProperties;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_RETRIEVE_SIZE_KEY;

@Slf4j
@Setter
@ToString
public abstract class AbstractTextAgent<C, S extends Flux<?>> implements TextAgent<C, S> {
    @Getter
    private final ChatClient chatClient;
    @Getter
    private final List<String> permittedFunctions;
    @Getter
    private final MessageProcessingStrategy<C> callMessageProcessingStrategy;
    @Getter
    private final MessageProcessingStrategy<S> streamMessageProcessingStrategy;
    @Getter
    private final Map<String, String> promptsList;
    @Value("classpath:/templates/PromptAndFunctionSuggestionsPrompt.txt")
    protected Resource promptAndFunctionSuggestionsPrompt;
    @Setter
    private Predicate<AIMessageDTO> supportsPredicate;

    protected AbstractTextAgent(ChatClient chatClient, List<String> permittedFunctions,
                                MessageProcessingStrategy<C> callMessageProcessingStrategy,
                                MessageProcessingStrategy<S> streamMessageProcessingStrategy,
                                Map<String, String> promptsList) {
        this.chatClient = chatClient;
        this.permittedFunctions = permittedFunctions;
        this.callMessageProcessingStrategy = callMessageProcessingStrategy;
        this.streamMessageProcessingStrategy = streamMessageProcessingStrategy;
        this.promptsList = promptsList;
    }

    /**
     * Retrieves the required functions based on the given message.
     *
     * @param message The user message
     * @return ChatClientSuggestedProperties
     */
    protected ChatClientSuggestedProperties getSuggestedChatClientProperties(String message) {
        log.debug("Getting required properties for message: {}", message);

        ChatClientSuggestedProperties properties = getChatClient().prompt()
                .user(u -> u.text(promptAndFunctionSuggestionsPrompt)
                        .param("promptsList", promptsList.toString())
                        .param("functionsList", permittedFunctions.toString())
                        .param("message", message))
                .advisors(a -> a.param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 0))
                .call()
                .entity(ChatClientSuggestedProperties.class);

        log.info("promptKey: {}", properties.getSystemPrompt());

        // setting the suggested prompt content
        String suggestedPrompt = promptsList.get(properties.getSystemPrompt());
        properties.setSystemPrompt(suggestedPrompt);

        log.info("""
                Required properties received from AI suggestion:
                Chosen prompt: {};
                Chosen functions: {}""", suggestedPrompt, properties.getFunctionNames());
        return properties;
    }

    @Override
    public boolean supports(AIMessageDTO inputMessage) {
        return supportsPredicate.test(inputMessage);
    }
}
