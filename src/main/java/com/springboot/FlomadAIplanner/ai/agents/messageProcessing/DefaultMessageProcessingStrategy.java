package com.springboot.FlomadAIplanner.ai.agents.messageProcessing;

import com.springboot.FlomadAIplanner.ai.agents.Agent;
import com.springboot.FlomadAIplanner.ai.agents.FunctionNameSuggestion;
import com.springboot.FlomadAIplanner.ai.dto.AIMessageDTO;
import org.springframework.core.io.Resource;

import java.util.List;

public class DefaultMessageProcessingStrategy implements MessageProcessingStrategy<String> {
    @Override
    public String processMessage(AIMessageDTO message, Agent agent, String prompt) {
        String mes = message.getMessage();
        List<FunctionNameSuggestion> requiredFunctions = agent.getRequiredFunctions(mes);

        if (requiredFunctions.isEmpty()) {
            return agent.getChatClient().prompt()
                    .system(prompt)
                    .user(mes)
                    .call()
                    .content();
        } else {
            return agent.getChatClient().prompt()
                    .system(prompt)
                    .user(mes)
                    .functions(agent.getFunctions(mes))
                    .call()
                    .content();
        }
    }

    @Override
    public String processMessage(AIMessageDTO message, Agent agent, Resource prompt) {
        String mes = message.getMessage();
        List<FunctionNameSuggestion> requiredFunctions = agent.getRequiredFunctions(mes);

        if (requiredFunctions.isEmpty()) {
            return agent.getChatClient().prompt()
                    .system(prompt)
                    .user(mes)
                    .call()
                    .content();
        } else {
            return agent.getChatClient().prompt()
                    .system(prompt)
                    .user(mes)
                    .functions(agent.getFunctions(mes))
                    .call()
                    .content();
        }
    }
}