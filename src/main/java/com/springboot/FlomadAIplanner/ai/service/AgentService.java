package com.springboot.FlomadAIplanner.ai.service;

import com.springboot.FlomadAIplanner.ai.agents.Agent;
import com.springboot.FlomadAIplanner.ai.dto.AIMessageDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
public class AgentService {

    private final Agent<String> intentExtractor;
    private final Agent<Flux<String>> currentAgent;
    private final Agent<Flux<String>> historyAgent;
    @Autowired
    public AgentService(Agent<String> intentExtractor, Agent<Flux<String>> currentAgent, Agent<Flux<String>> historyAgent) {
        this.intentExtractor = intentExtractor;
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

    public String processIntentExtractor(AIMessageDTO message) {
        // Use the historyAgent to process the chat messages
        return intentExtractor.processMessage(message);
    }
}
