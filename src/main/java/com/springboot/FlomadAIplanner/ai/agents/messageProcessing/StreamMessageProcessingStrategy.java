package com.springboot.FlomadAIplanner.ai.agents.messageProcessing;

import com.springboot.FlomadAIplanner.ai.agents.Agent;
import com.springboot.FlomadAIplanner.ai.agents.FunctionNameSuggestion;
import com.springboot.FlomadAIplanner.ai.dto.AIMessageDTO;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;

@Component
public class StreamMessageProcessingStrategy implements MessageProcessingStrategy<Flux<String>> {

    @Override
    public Flux<String> processMessage(AIMessageDTO message,
                                       Agent<Flux<String>> agent,
                                       PromptTemplate promptTemplate)
    {
        Prompt p = promptTemplate.create(Map.of("userInput", message.getMessage()));
        List<FunctionNameSuggestion> requiredFunctions = agent.getRequiredFunctions(p.getContents());

        if (requiredFunctions.isEmpty()) {
            return agent.getChatClient().prompt()
                    .messages(p.getInstructions())
                    .stream()
                    .content();
        } else {
            return agent.getChatClient().prompt()
                    .messages(p.getInstructions())
                    .functions(agent.getFunctions(requiredFunctions))
                    .stream()
                    .content();
        }
    }
}