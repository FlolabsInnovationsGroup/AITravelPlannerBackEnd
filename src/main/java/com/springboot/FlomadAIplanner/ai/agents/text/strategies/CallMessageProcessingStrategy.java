package com.springboot.FlomadAIplanner.ai.agents.messageProcessing;

import com.springboot.FlomadAIplanner.ai.agents.Agent;
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
public class CallMessageProcessingStrategy implements MessageProcessingStrategy<String> {

    @Override
    public String processMessage(MessageProcessingStrategyDTO messageProcessingStrategyDTO) {
        log.debug("Processing message: {}", messageProcessingStrategyDTO.getAiMessage().getMessage());

        AIMessageDTO aiMessage = messageProcessingStrategyDTO.getAiMessage();
        Agent agent = messageProcessingStrategyDTO.getAgent();  // The selected agent
        List<String> requiredFunctions = messageProcessingStrategyDTO.getRequiredFunctions();
        Prompt systemPrompt = messageProcessingStrategyDTO.getSystemPrompt();

        // Determine if this is an audio message
        if (aiMessage.getMedia() != null && aiMessage.getMedia().getContentType().startsWith("audio/")) {
            log.info("Processing audio message...");
            // If it's an audio message, let the agent handle audio processing
            return (String) agent.processCallMessage(aiMessage);

            // Otherwise, process text messages
        } else if (agent instanceof TextAgentImpl) {
            log.info("Processing text message...");
            // Text message processing using the agent’s chat client
            return ((TextAgentImpl<?, ?>) agent).getChatClient().prompt()
                    .system(systemPrompt.getContents())
                    .user(aiMessage.getMessage())
                    .advisors(a -> a.param(CHAT_MEMORY_CONVERSATION_ID_KEY, aiMessage.getSessionId()))
                    .functions(requiredFunctions.toArray(new String[0]))
                    .call()
                    .content();
        } else {
            throw new IllegalArgumentException("Unsupported message type");
        }
    }
}


