package com.springboot.FlomadAIplanner.ai.agents.text.strategies;

import com.springboot.FlomadAIplanner.ai.agents.MessageProcessingStrategy;
import com.springboot.FlomadAIplanner.ai.dto.AIMessageDTO;
import com.springboot.FlomadAIplanner.ai.dto.MessageProcessingStrategyDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Component;

import java.util.List;

import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY;

@Slf4j
@Component
public class CallMessageProcessingStrategy implements MessageProcessingStrategy<MessageProcessingStrategyDTO, String> {

    @Override
    public String processMessage(MessageProcessingStrategyDTO messageProcessingStrategyDTO) {
        log.debug("Processing message: {}", messageProcessingStrategyDTO.getAiMessage().getMessage());

        AIMessageDTO aiMessage = messageProcessingStrategyDTO.getAiMessage();
        ChatClient client = messageProcessingStrategyDTO.getClient();
        List<String> requiredFunctions = messageProcessingStrategyDTO.getRequiredFunctions();
        Prompt systemPrompt = messageProcessingStrategyDTO.getSystemPrompt();

        return client.prompt()
                .system(systemPrompt.getContents())
                .user(aiMessage.getMessage())
                .advisors(a -> a.param(CHAT_MEMORY_CONVERSATION_ID_KEY, aiMessage.getSessionId()))
                .functions(requiredFunctions.toArray(new String[0]))
                .call()
                .content();
    }

}