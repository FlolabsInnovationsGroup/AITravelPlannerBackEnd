package com.springboot.FlomadAIplanner.ai.factory;

import com.springboot.FlomadAIplanner.ai.agents.text.TextAgentImpl;
import com.springboot.FlomadAIplanner.ai.config.cnst.AIModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
public class AgentFactory {

    private final Map<AIModel, TextAgentImpl<?, ?>> chatAgents;

    @Autowired
    public AgentFactory(@Qualifier("chatAgents") Map<AIModel, TextAgentImpl<?, ?>> chatAgents) {
        log.info("ChatClientFactory initialized with chat clients: {}", chatAgents);
        this.chatAgents = chatAgents;
    }

    public TextAgentImpl<?, ?> getAgent(AIModel aiModel) {
        TextAgentImpl<?, ?> agent = chatAgents.get(aiModel);
        if (agent == null) {
            throw new IllegalArgumentException("Unknown model: " + aiModel.getModelName());
        }
        return agent;
    }
}
