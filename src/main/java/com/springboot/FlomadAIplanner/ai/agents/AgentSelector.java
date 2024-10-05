package com.springboot.FlomadAIplanner.ai.agents;

import com.springboot.FlomadAIplanner.ai.dto.AIMessageDTO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AgentSelector {

    private final List<Agent> agents;

    public AgentSelector(List<Agent> agents) {
        this.agents = agents;
    }

    public Agent selectAgent(AIMessageDTO inputMessage) {
        return agents.stream()
                .filter(agent -> agent.supports(inputMessage))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No suitable agent found for the given input and output type"));
    }
}
