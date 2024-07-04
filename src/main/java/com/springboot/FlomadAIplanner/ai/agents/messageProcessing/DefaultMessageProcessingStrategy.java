package com.springboot.FlomadAIplanner.ai.agents.messageProcessing;

import com.springboot.FlomadAIplanner.ai.agents.Agent;
import com.springboot.FlomadAIplanner.ai.agents.FunctionAnnotationUtils;
import com.springboot.FlomadAIplanner.ai.agents.FunctionNameSuggestion;
import com.springboot.FlomadAIplanner.ai.dto.AIMessageDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class DefaultMessageProcessingStrategy implements MessageProcessingStrategy<String> {

    @Override
    public String processMessage(AIMessageDTO message,
                                 Agent<String> agent,
                                 PromptTemplate promptTemplate)
    {
        Prompt p = promptTemplate.create(Map.of("userInput", message.getMessage()));
        log.info("Prompts: {}", p.getContents());
        List<FunctionNameSuggestion> requiredFunctions = agent.getRequiredFunctions(p.getContents());
        log.info("requiredFunctions: {}", requiredFunctions);

        if (requiredFunctions.isEmpty()) {
            return agent.getChatClient().prompt()
                    .messages(p.getInstructions())
                    .call()
                    .content();
        } else {
            Map<String, Object> result =  agent.getChatClient().prompt()
                    .messages(p.getInstructions())
                    .functions(agent.getFunctions(requiredFunctions))
                    .call()
                    .entity(new ParameterizedTypeReference<Map<String, Object>>() {
                    });
            return FunctionAnnotationUtils.mapToString(result);
        }
    }
}