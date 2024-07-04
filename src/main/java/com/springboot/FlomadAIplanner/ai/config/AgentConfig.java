package com.springboot.FlomadAIplanner.ai.config;

import com.springboot.FlomadAIplanner.ai.agents.Agent;
import com.springboot.FlomadAIplanner.ai.agents.FunctionAnnotationUtils;
import com.springboot.FlomadAIplanner.ai.agents.messageProcessing.StreamMessageProcessingStrategy;
import com.springboot.FlomadAIplanner.ai.functions.DistanceCalculatorFunction;
import com.springboot.FlomadAIplanner.ai.functions.FlightSearchFunction;
import org.springframework.ai.autoconfigure.chat.client.ChatClientBuilderConfigurer;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.PromptChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import reactor.core.publisher.Flux;

@Configuration
public class AgentConfig {

    public static final String CURRENT_AGENT = "current";
    public static final String HISTORY_AGENT = "history";
    @Value("classpath:/templates/DefaultPrompt.txt")
    private Resource promptResource;
    @Value("classpath:/templates/IntentExtractorSystemPrompt.txt")
    private Resource IntentExtractorResource;
    @Value("classpath:/templates/TestChatPrompt.txt")
    protected Resource chatPrompt;

    @Bean
    public ChatMemory currentMemory() {
        return new InMemoryChatMemory();
    }
    @Bean
    public ChatMemory historyMemory() {
        return new InMemoryChatMemory();
    }

//    @Bean
//    @Scope("prototype")
//    @ConditionalOnMissingBean
    public ChatClient.Builder chatClientBuilder(ChatClientBuilderConfigurer chatClientBuilderConfigurer,
                                                @Qualifier("openAiChatModel") ChatModel chatModel) {
        ChatClient.Builder builder = ChatClient.builder(chatModel);
        return chatClientBuilderConfigurer.configure(builder);
    }

    @Bean
    public Agent<Flux<String>> IntentExtractor(@Qualifier("openAiChatModel") ChatModel chatModel, ChatMemory chatMemory) {
        return new Agent.Builder<Flux<String>>().setChatClient(
                        ChatClient.builder(chatModel)
                                .defaultSystem(IntentExtractorResource)
                                .defaultAdvisors(new PromptChatMemoryAdvisor(chatMemory))
                                .build()
                ).setFunctions(
                        FunctionAnnotationUtils.extract(
                                DistanceCalculatorFunction.class,
                                FlightSearchFunction.class
                        )
                ).setPrompt(chatPrompt)
                .setMessageProcessingStrategy(
                        new StreamMessageProcessingStrategy()
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
                    FunctionAnnotationUtils.extract(
                        DistanceCalculatorFunction.class,
                        FlightSearchFunction.class
                    )
                ).setPrompt(chatPrompt)
                .setMessageProcessingStrategy(
                        new StreamMessageProcessingStrategy()
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
                        FunctionAnnotationUtils.extract(
                                DistanceCalculatorFunction.class,
                                FlightSearchFunction.class
                        )
                ).setPrompt(chatPrompt)
                .setMessageProcessingStrategy(
                        new StreamMessageProcessingStrategy()
                ).build();
    }

//    @Bean
    public ChatClient geminiChatClient(@Qualifier("vertexAiGeminiChat") ChatModel chatModel, ChatMemory chatMemory) {
        return ChatClient.builder(chatModel)
                .defaultSystem(promptResource)
                .defaultAdvisors(new PromptChatMemoryAdvisor(chatMemory))
                .build();
    }

}