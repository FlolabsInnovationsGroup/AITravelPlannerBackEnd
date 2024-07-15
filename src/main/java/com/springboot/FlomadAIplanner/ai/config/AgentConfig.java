package com.springboot.FlomadAIplanner.ai.config;

import com.springboot.FlomadAIplanner.ai.agents.messageProcessing.CallMessageProcessingStrategy;
import com.springboot.FlomadAIplanner.ai.agents.messageProcessing.StreamMessageProcessingStrategy;
import com.springboot.FlomadAIplanner.ai.agents.text.TextAgentImpl;
import com.springboot.FlomadAIplanner.ai.config.cnst.AIModel;
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
     * Agent for OpenAI
     */
    @Bean
    @Qualifier("openAiAgent")
    public TextAgentImpl<String, Flux<String>> openAiAgent(@Qualifier("openAiChatClient") ChatClient openAiChatClient,
                                                           @Qualifier("aiFunctionDescriptions") List<String> allFunctionDescriptions,
                                                           @Qualifier("templatePrompts") Map<String, String> promptsList) {
        return new TextAgentImpl<>(openAiChatClient, allFunctionDescriptions,
                callMessageProcessingStrategy, streamMessageProcessingStrategy, promptsList);
    }

    /**
     * Agent for Gemini
     */
    @Bean
    @Qualifier("geminiAiAgent")
    public TextAgentImpl<String, Flux<String>> geminiAiAgent(@Qualifier("geminiChatClient") ChatClient geminiChatClient,
                                                             @Qualifier("aiFunctionDescriptions") List<String> allFunctionDescriptions,
                                                             @Qualifier("templatePrompts") Map<String, String> promptsList) {
        return new TextAgentImpl<>(geminiChatClient, allFunctionDescriptions,
                callMessageProcessingStrategy, streamMessageProcessingStrategy, promptsList);
    }

    @Bean
    @Qualifier("chatAgents")
    public Map<AIModel, TextAgentImpl<?, ?>> chatAgents(
            @Qualifier("openAiAgent") TextAgentImpl<?, ?> openAiAgent,
            @Qualifier("geminiAiAgent") TextAgentImpl<?, ?> geminiAiAgent) {
        return Map.of(
                AIModel.OPENAI_MODEL, openAiAgent,
                AIModel.GEMINI_MODEL, geminiAiAgent
        );
    }

}