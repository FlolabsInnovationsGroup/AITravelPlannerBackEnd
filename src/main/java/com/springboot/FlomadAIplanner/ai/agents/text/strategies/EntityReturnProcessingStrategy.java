package com.springboot.FlomadAIplanner.ai.agents.text.strategies;

import com.springboot.FlomadAIplanner.ai.agents.MessageProcessingStrategy;
import com.springboot.FlomadAIplanner.ai.agents.text.TextAgentImpl;
import com.springboot.FlomadAIplanner.ai.dto.AIMessageDTO;
import com.springboot.FlomadAIplanner.ai.dto.MessageProcessingStrategyDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Component;

import java.util.List;

import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY;

@Slf4j
@Component
public class EntityReturnProcessingStrategy<T> implements MessageProcessingStrategy<T> {

    @Override
    public T processMessage(MessageProcessingStrategyDTO<T> messageProcessingStrategyDTO) {
        log.debug("Processing message: {}", messageProcessingStrategyDTO.getAiMessage().getMessage());

        AIMessageDTO aiMessage = messageProcessingStrategyDTO.getAiMessage();

        // Cast the Agent to TextAgentImpl, assuming it's always this type in this context.
        TextAgentImpl<?, ?> agent = (TextAgentImpl<?, ?>) messageProcessingStrategyDTO.getAgent();

        List<String> requiredFunctions = messageProcessingStrategyDTO.getRequiredFunctions();
        Prompt systemPrompt = messageProcessingStrategyDTO.getSystemPrompt();

        Class<T> clazz = messageProcessingStrategyDTO.getReturnClassType();

        return agent.getChatClient().prompt()
                .system(systemPrompt.getContents())
                .user(aiMessage.getMessage())
                .advisors(a -> a.param(CHAT_MEMORY_CONVERSATION_ID_KEY, aiMessage.getSessionId()))
                .functions(requiredFunctions.toArray(new String[0]))
                .call()
                .entity(clazz);
    }
}

