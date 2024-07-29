package com.springboot.FlomadAIplanner.ai.factory;

import com.springboot.FlomadAIplanner.ai.agents.text.TextAgent;
import com.springboot.FlomadAIplanner.ai.config.cnst.AIModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
public class AgentFactory {

    private final Map<AIModel, TextAgent<?, ?>> chatAgents;

    public AgentFactory(@Qualifier("chatAgents") Map<AIModel, TextAgent<?, ?>> chatAgents) {
        log.info("ChatClientFactory initialized with chat clients: {}", chatAgents);
        this.chatAgents = chatAgents;
    }

    public TextAgent<?, ?> getAgent(AIModel aiModel) {
        TextAgent<?, ?> agent = chatAgents.get(aiModel);
        if (agent == null) {
            throw new IllegalArgumentException("Unknown model: " + aiModel.getModelName());
        }
        return agent;
    }
}
