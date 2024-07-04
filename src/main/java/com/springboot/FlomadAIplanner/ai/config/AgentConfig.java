package com.springboot.FlomadAIplanner.ai.config;

import com.springboot.FlomadAIplanner.ai.agents.Agent;
import com.springboot.FlomadAIplanner.ai.agents.FunctionAnnotationUtils;
import com.springboot.FlomadAIplanner.ai.agents.messageProcessing.DefaultMessageProcessingStrategy;
import com.springboot.FlomadAIplanner.ai.agents.messageProcessing.IntentExtractorProcessingStrategy;
import com.springboot.FlomadAIplanner.ai.agents.messageProcessing.StreamMessageProcessingStrategy;
import com.springboot.FlomadAIplanner.ai.functions.*;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.PromptChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.Map;

@Configuration
public class AgentConfig {
    @Value("classpath:/templates/DefaultPrompt.txt")
    private Resource promptResource;
    @Value("classpath:/templates/IntentExtractorSystemPrompt.txt")
    private Resource IntentExtractorSystem;
    @Value("classpath:/templates/IntentExtractorAssistantPrompt.txt")
    private Resource IntentExtractorAssistant;
    @Value("classpath:/templates/TestChatPrompt.txt")
    protected Resource chatPrompt;
    @Autowired
    private DefaultMessageProcessingStrategy defaultMessageProcessingStrategy;
    @Autowired
    private StreamMessageProcessingStrategy streamMessageProcessingStrategy;
    @Autowired
    private IntentExtractorProcessingStrategy intentExtractorProcessingStrategy;

    @Bean
    public ChatMemory currentMemory() {
        return new InMemoryChatMemory();
    }
    @Bean
    public ChatMemory historyMemory() {
        return new InMemoryChatMemory();
    }


    @Bean
    public Agent<String> intentExtractor(@Qualifier("openAiChatModel") ChatModel chatModel, ChatMemory chatMemory) {
        return new Agent.Builder<String>().setChatClient(
                        ChatClient.builder(chatModel)
                                .defaultSystem(IntentExtractorSystem)
                                .defaultAdvisors(new PromptChatMemoryAdvisor(chatMemory))
                                .build()
                ).setFunctions(
                    new ArrayList<>(FunctionAnnotationUtils.extract(
                            CurrentTimeFunction.class,
                            FlightSearchFunctionMock.class
                    ))
                ).setPrompt(IntentExtractorAssistant)
                .setMessageProcessingStrategy(
                        intentExtractorProcessingStrategy
                ).build();
    }

    @Bean
    public Agent<Flux<String>> currentAgent(@Qualifier("openAiChatModel") ChatModel chatModel, ChatMemory currentMemory) {
        return new Agent.Builder<Flux<String>>().setChatClient(
                        ChatClient.builder(chatModel)
                                .defaultSystem(promptResource)
                                .defaultAdvisors(new PromptChatMemoryAdvisor(currentMemory))
                                .build()
                ).setFunctions(
                    new ArrayList<>(FunctionAnnotationUtils.extract(
                        DistanceCalculatorFunctionMock.class,
                        FlightSearchFunctionMock.class
                    ))
                ).setPrompt(chatPrompt)
                .setMessageProcessingStrategy(
                        streamMessageProcessingStrategy
                ).build();
    }

    @Bean
    public Agent<Flux<String>> historyAgent(@Qualifier("openAiChatModel") ChatModel chatModel, ChatMemory historyMemory) {
        return new Agent.Builder<Flux<String>>().setChatClient(
                        ChatClient.builder(chatModel)
                                .defaultSystem(promptResource)
                                .defaultAdvisors(new PromptChatMemoryAdvisor(historyMemory))
                                .build()
                ).setFunctions(
                    new ArrayList<>(FunctionAnnotationUtils.extract(
                            DistanceCalculatorFunctionMock.class,
                            FlightSearchFunctionMock.class
                    ))
                ).setPrompt(chatPrompt)
                .setMessageProcessingStrategy(
                        streamMessageProcessingStrategy
                ).build();
    }

}