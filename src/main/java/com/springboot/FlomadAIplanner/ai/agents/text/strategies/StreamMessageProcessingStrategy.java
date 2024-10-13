package com.springboot.FlomadAIplanner.ai.agents.messageProcessing;

import com.springboot.FlomadAIplanner.ai.agents.text.TextAgentImpl;
import com.springboot.FlomadAIplanner.ai.dto.AIMessageDTO;
import com.springboot.FlomadAIplanner.ai.dto.MessageProcessingStrategyDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.List;

import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY;

@Slf4j
@Component
public class StreamMessageProcessingStrategy implements MessageProcessingStrategy<Flux<String>> {

    @Override
    @SuppressWarnings("unchecked")
    public Flux<String> processMessage(MessageProcessingStrategyDTO<Flux<String>> messageProcessingStrategyDTO) {
        log.debug("Processing message: {}", messageProcessingStrategyDTO.getAiMessage().getMessage());

        AIMessageDTO aiMessage = messageProcessingStrategyDTO.getAiMessage();

        // Safely suppress unchecked cast warning
        TextAgentImpl<Flux<String>, ?> agent = (TextAgentImpl<Flux<String>, ?>) messageProcessingStrategyDTO.getAgent();

        List<String> requiredFunctions = messageProcessingStrategyDTO.getRequiredFunctions();
        Prompt systemPrompt = messageProcessingStrategyDTO.getSystemPrompt();

        return agent.getChatClient().prompt()
                .system(systemPrompt.getContents())
                .user(aiMessage.getMessage())
                .advisors(a -> a.param(CHAT_MEMORY_CONVERSATION_ID_KEY, aiMessage.getSessionId()))
                .functions(requiredFunctions.toArray(new String[0]))
                .stream()
                .content();
    }
}

