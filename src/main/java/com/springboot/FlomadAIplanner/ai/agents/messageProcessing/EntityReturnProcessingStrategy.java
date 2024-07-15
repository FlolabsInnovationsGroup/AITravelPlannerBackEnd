package com.springboot.FlomadAIplanner.ai.agents.messageProcessing;

import com.springboot.FlomadAIplanner.ai.agents.TextTextAgentImpl;
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
    public T processMessage(MessageProcessingStrategyDTO messageProcessingStrategyDTO) {
        log.debug("Processing message: {}", messageProcessingStrategyDTO.getAiMessage().getMessage());

        AIMessageDTO aiMessage = messageProcessingStrategyDTO.getAiMessage();
        TextTextAgentImpl<?, ?> agent = messageProcessingStrategyDTO.getAgent();
        List<String> requiredFunctions = messageProcessingStrategyDTO.getRequiredFunctions();
        Prompt systemPrompt = messageProcessingStrategyDTO.getSystemPrompt();

        Class<T> clazz = getReturnClassType(messageProcessingStrategyDTO.getReturnClassType());

        return agent.getChatClient().prompt()
                .system(systemPrompt.getContents())
                .user(aiMessage.getMessage())
                .advisors(a -> a.param(CHAT_MEMORY_CONVERSATION_ID_KEY, aiMessage.getSessionId()))
                .functions(requiredFunctions.toArray(new String[0]))
                .call()
                .entity(clazz);
    }

    @SuppressWarnings("unchecked")
    private Class<T> getReturnClassType(Class<?> clazz) {
        log.debug("Getting return class type: {}", clazz.getName());
        return (Class<T>) clazz;
    }

}
