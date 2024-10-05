package com.springboot.FlomadAIplanner.ai.config;

import com.springboot.FlomadAIplanner.ai.agents.text.TextAgent;
import com.springboot.FlomadAIplanner.ai.agents.text.TextAgentImpl;
import com.springboot.FlomadAIplanner.ai.agents.text.strategies.CallMessageProcessingStrategy;
import com.springboot.FlomadAIplanner.ai.agents.text.strategies.StreamMessageProcessingStrategy;
import com.springboot.FlomadAIplanner.ai.config.cnst.AIModel;
import com.springboot.FlomadAIplanner.ai.dto.OutputType;
import lombok.AllArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;

@Configuration
@AllArgsConstructor
public class AgentConfig {

//    =================== TEXT AGENTS ===================

    /**
     * Agent for OpenAI
     */
    @Bean
    @Qualifier("openAiAgent")
    public TextAgent<String, Flux<String>> openAiAgent(@Qualifier("openAiChatClient") ChatClient openAiChatClient,
                                                       @Qualifier("aiFunctionDescriptions") List<String> allFunctionDescriptions,
                                                       @Qualifier("templatePrompts") Map<String, String> promptsList,
                                                       CallMessageProcessingStrategy callMessageProcessingStrategy,
                                                       StreamMessageProcessingStrategy streamMessageProcessingStrategy) {
        TextAgentImpl<String, Flux<String>> agent = new TextAgentImpl<>(openAiChatClient, allFunctionDescriptions,
                callMessageProcessingStrategy, streamMessageProcessingStrategy, promptsList);
        agent.setSupportsPredicate(inputMessage -> {
            boolean isTextMessage = inputMessage.getMedia() == null && inputMessage.getMessage() != null;
            boolean isOutputTypeText = inputMessage.getOutputType() == OutputType.TEXT;
            return isTextMessage && isOutputTypeText;
        });

        return agent;
    }

    /**
     * Agent for Gemini
     */
    @Bean
    @Qualifier("geminiAiAgent")
    public TextAgent<String, Flux<String>> geminiAiAgent(@Qualifier("geminiChatClient") ChatClient geminiChatClient,
                                                         @Qualifier("aiFunctionDescriptions") List<String> allFunctionDescriptions,
                                                         @Qualifier("templatePrompts") Map<String, String> promptsList,
                                                         CallMessageProcessingStrategy callMessageProcessingStrategy,
                                                         StreamMessageProcessingStrategy streamMessageProcessingStrategy) {
        TextAgentImpl<String, Flux<String>> agent = new TextAgentImpl<>(geminiChatClient, allFunctionDescriptions,
                callMessageProcessingStrategy, streamMessageProcessingStrategy, promptsList);
        agent.setSupportsPredicate(inputMessage -> false);

        return agent;
    }


//    =================== CHAT AGENTS ===================

    @Bean
    @Qualifier("chatAgents")
    public Map<AIModel, TextAgent<?, ?>> chatAgents(
            @Qualifier("openAiAgent") TextAgent<?, ?> openAiAgent,
            @Qualifier("geminiAiAgent") TextAgent<?, ?> geminiAiAgent) {
        return Map.of(
                AIModel.OPENAI_MODEL, openAiAgent,
                AIModel.GEMINI_MODEL, geminiAiAgent
        );
    }

}