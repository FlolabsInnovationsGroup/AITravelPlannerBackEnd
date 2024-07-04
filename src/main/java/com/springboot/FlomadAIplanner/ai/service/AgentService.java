package com.springboot.FlomadAIplanner.ai.service;

import com.springboot.FlomadAIplanner.ai.agents.Agent;
import com.springboot.FlomadAIplanner.ai.dto.AIMessageDTO;
import com.springboot.FlomadAIplanner.ai.factory.ChatClientFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.List;

@Service
public class AgentService {
    private final Agent<Flux<String>> currentAgent;
    private final Agent<Flux<String>> historyAgent;

    @Autowired
    public AgentService(@Qualifier("currentAgent") Agent<Flux<String>> currentAgent,
                       @Qualifier("historyAgent") Agent<Flux<String>> historyAgent) {
        this.currentAgent = currentAgent;
        this.historyAgent = historyAgent;
    }

    public Flux<String> processCurrentChat(AIMessageDTO message) {
        // Use the currentAgent to process the chat messages
        return currentAgent.processMessage(message, "");
    }

    public Flux<String> processHistoryChat(AIMessageDTO message) {
        // Use the historyAgent to process the chat messages
        return historyAgent.processMessage(message, "");
    }
}
