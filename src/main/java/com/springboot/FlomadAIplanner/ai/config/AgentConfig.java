package com.springboot.FlomadAIplanner.ai.config;


import com.springboot.FlomadAIplanner.ai.agents.Agent;
import com.springboot.FlomadAIplanner.ai.agents.audio.AudioAgentImpl;
import com.springboot.FlomadAIplanner.ai.agents.audio.AudioTranscriptionService;
import com.springboot.FlomadAIplanner.ai.agents.text.TextAgent;
import com.springboot.FlomadAIplanner.ai.agents.text.TextAgentImpl;
import com.springboot.FlomadAIplanner.ai.agents.text.strategies.CallMessageProcessingStrategy;
import com.springboot.FlomadAIplanner.ai.agents.text.strategies.StreamMessageProcessingStrategy;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;

@Configuration
public class AgentConfig {
    private final CallMessageProcessingStrategy callMessageProcessingStrategy;
    private final StreamMessageProcessingStrategy streamMessageProcessingStrategy;

    public AgentConfig(CallMessageProcessingStrategy callMessageProcessingStrategy, StreamMessageProcessingStrategy streamMessageProcessingStrategy) {
        this.callMessageProcessingStrategy = callMessageProcessingStrategy;
        this.streamMessageProcessingStrategy = streamMessageProcessingStrategy;
    }


    /**
     * Agent for OpenAI (Text Agent)
     */
    @Bean
    @Qualifier("openAiAgent")
    public TextAgent<String, Flux<String>> openAiAgent(@Qualifier("openAiChatClient") ChatClient openAiChatClient,
                                                       @Qualifier("aiFunctionDescriptions") List<String> allFunctionDescriptions,
                                                       @Qualifier("templatePrompts") Map<String, String> promptsList) {
        return new TextAgentImpl<>(openAiChatClient, allFunctionDescriptions,
                callMessageProcessingStrategy, streamMessageProcessingStrategy, promptsList);
    }

    /**
     * Agent for Gemini (Text Agent)
     */
    @Bean
    @Qualifier("geminiAiAgent")
    public TextAgent<String, Flux<String>> geminiAiAgent(@Qualifier("geminiChatClient") ChatClient geminiChatClient,
                                                         @Qualifier("aiFunctionDescriptions") List<String> allFunctionDescriptions,
                                                         @Qualifier("templatePrompts") Map<String, String> promptsList) {
        return new TextAgentImpl<>(geminiChatClient, allFunctionDescriptions,
                callMessageProcessingStrategy, streamMessageProcessingStrategy, promptsList);
    }

    /**
     * Agent for Audio Processing (Audio Agent)
     */
    @Bean
    @Qualifier("openAiAudioAgent")
    public Agent audioAgent(@Qualifier("openAiAgent") TextAgent<String, Flux<String>> textAgent,
                            AudioTranscriptionService transcriptionModel) {
        return new AudioAgentImpl(textAgent, transcriptionModel);
    }

    @Bean
    @Qualifier("chatAgents")
    public List<Agent> agents(@Qualifier("openAiAgent") Agent openAiAgent,
                              @Qualifier("geminiAiAgent") Agent geminiAiAgent,
                              @Qualifier("audioAgent") Agent audioAgent) {
        return List.of(openAiAgent, geminiAiAgent, audioAgent);
    }

}

