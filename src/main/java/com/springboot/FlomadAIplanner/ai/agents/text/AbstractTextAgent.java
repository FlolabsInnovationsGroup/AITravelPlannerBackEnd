package com.springboot.FlomadAIplanner.ai.agents.text;

import com.springboot.FlomadAIplanner.ai.agents.MessageProcessingStrategy;
import com.springboot.FlomadAIplanner.ai.dto.AIMessageDTO;
import com.springboot.FlomadAIplanner.ai.dto.ChatClientSuggestedProperties;
import com.springboot.FlomadAIplanner.ai.dto.MessageProcessingStrategyDTO;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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
    private final MessageProcessingStrategy<MessageProcessingStrategyDTO, C> callMessageProcessingStrategy;
    @Getter
    private final MessageProcessingStrategy<MessageProcessingStrategyDTO, S> streamMessageProcessingStrategy;
    @Getter
    private final Map<String, String> promptsList;
    @Getter
    private final Map<String, String> promptMetadata;
    @Value("classpath:/templates/LightweightPromptSelection.txt")
    protected Resource lightweightPromptSelectionTemplate;
    @Setter
    private Predicate<AIMessageDTO> supportsPredicate = (m) -> false;

    protected AbstractTextAgent(ChatClient chatClient, List<String> permittedFunctions,
                                MessageProcessingStrategy<MessageProcessingStrategyDTO, C> callMessageProcessingStrategy,
                                MessageProcessingStrategy<MessageProcessingStrategyDTO, S> streamMessageProcessingStrategy,
                                Map<String, String> promptsList,
                                Map<String, String> promptMetadata) {
        this.chatClient = chatClient;
        this.permittedFunctions = permittedFunctions;
        this.callMessageProcessingStrategy = callMessageProcessingStrategy;
        this.streamMessageProcessingStrategy = streamMessageProcessingStrategy;
        this.promptsList = promptsList;
        this.promptMetadata = promptMetadata;
    }

    /**
     * Retrieves the required functions based on the given message using lightweight prompt selection.
     * This method sends only prompt metadata (not full content) to OpenAI for efficient selection.
     *
     * @param message The user message
     * @return ChatClientSuggestedProperties
     */
    protected ChatClientSuggestedProperties getSuggestedChatClientProperties(String message) {
        log.debug("Getting required properties for message using lightweight selection: {}", message);

        try {
            // Use lightweight selection with only metadata, not full prompt content
            ChatClientSuggestedProperties properties = getChatClient().prompt()
                    .user(u -> u.text(lightweightPromptSelectionTemplate)
                            .param("promptMetadata", promptMetadata.toString())
                            .param("functionsList", permittedFunctions.toString())
                            .param("message", message))
                    .advisors(a -> a.param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 0))
                    .call()
                    .entity(ChatClientSuggestedProperties.class);

            log.info("Selected prompt key: {}", properties.getSystemPrompt());

            // Now load only the selected prompt content (not all prompts)
            String suggestedPrompt = promptsList.get(properties.getSystemPrompt());
            if (suggestedPrompt == null) {
                log.warn("Selected prompt '{}' not found in promptsList. Falling back to SingleQuestionPrompt.txt", 
                        properties.getSystemPrompt());
                suggestedPrompt = promptsList.get("SingleQuestionPrompt.txt");
                properties.setSystemPrompt("SingleQuestionPrompt.txt");
            }
            properties.setSystemPrompt(suggestedPrompt);

            log.info("""
                    Lightweight selection completed:
                    Chosen prompt: {};
                    Chosen functions: {}""", properties.getSystemPrompt(), properties.getFunctionNames());
            
            return properties;
            
        } catch (Exception e) {
            log.error("Error in lightweight prompt selection, falling back to default: {}", e.getMessage());
            // Fallback to a safe default
            ChatClientSuggestedProperties fallback = new ChatClientSuggestedProperties();
            fallback.setSystemPrompt(promptsList.get("SingleQuestionPrompt.txt"));
            fallback.setFunctionNames(List.of());
            return fallback;
        }
    }

    @Override
    public boolean supports(AIMessageDTO inputMessage) {
        return supportsPredicate.test(inputMessage);
    }
}
