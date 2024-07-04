package com.springboot.FlomadAIplanner.ai.agents.messageProcessing;

import com.springboot.FlomadAIplanner.ai.agents.Agent;
import com.springboot.FlomadAIplanner.ai.agents.FunctionNameSuggestion;
import com.springboot.FlomadAIplanner.ai.dto.AIMessageDTO;
import org.springframework.ai.chat.prompt.Prompt;
import reactor.core.publisher.Flux;

import java.util.List;

public class StreamMessageProcessingStrategy implements MessageProcessingStrategy<Flux<String>> {
    @Override
    public Flux<String> processMessage(Agent agent, Prompt prompt) {
        String mes = message.getMessage();
        List<FunctionNameSuggestion> requiredFunctions = agent.getRequiredFunctions(mes);

        if (requiredFunctions.isEmpty()) {
            return agent.getChatClient().prompt()
                    .user(prompt)
                    .stream()
                    .content();
        } else {
            return agent.getChatClient().prompt()
                    .system(prompt)
                    .user(mes)
                    .functions(agent.getFunctions(mes))
                    .stream()
                    .content();
        }
    }
}