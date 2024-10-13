package com.springboot.FlomadAIplanner.ai.factory;

import com.springboot.FlomadAIplanner.ai.agents.Agent;
import com.springboot.FlomadAIplanner.ai.dto.AIMessageDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class AgentFactory {

    private final List<Agent> agents;

    @Autowired
    public AgentFactory(List<Agent> agents) {
        this.agents = agents;
    }

    public Agent getAgent(AIMessageDTO aiMessage) {
        // Loop through available agents and return the one that supports the message type
        return agents.stream()
                .filter(agent -> agent.supports(aiMessage))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No suitable agent found for message"));
    }
}
