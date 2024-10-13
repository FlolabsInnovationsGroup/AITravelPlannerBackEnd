package com.springboot.FlomadAIplanner.ai.config;

import com.springboot.FlomadAIplanner.ai.config.cnst.AIModel;
import org.springframework.ai.autoconfigure.chat.client.ChatClientBuilderConfigurer;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.PromptChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.core.io.Resource;

import java.util.Map;

@Configuration
public class ChatClientConfig {

    @Value("classpath:/templates/DefaultPrompt.txt")
    private Resource promptResource;

    @Bean
    @Qualifier("exceptionHandlingMemory")
    public ChatMemory exceptionHandlingMemory() {
        return new InMemoryChatMemory();
    }

    @Bean
    @Scope("prototype")
    @ConditionalOnMissingBean
    public ChatClient.Builder chatClientBuilder(ChatClientBuilderConfigurer chatClientBuilderConfigurer,
                                                @Qualifier("openAiChatModel") ChatModel chatModel) {
        ChatClient.Builder builder = ChatClient.builder(chatModel);
        return chatClientBuilderConfigurer.configure(builder);
    }


    @Bean
    public ChatClient openAiChatClient(@Qualifier("openAiChatModel") ChatModel chatModel,
                                       @Qualifier("customDatabaseChatMemory") ChatMemory chatMemory) {
        return ChatClient.builder(chatModel)
                .defaultSystem(promptResource)
                .defaultAdvisors(new PromptChatMemoryAdvisor(chatMemory))
                .build();
    }

    @Bean
    public ChatClient geminiChatClient(@Qualifier("vertexAiGeminiChat") ChatModel chatModel,
                                       @Qualifier("customDatabaseChatMemory") ChatMemory chatMemory) {
        return ChatClient.builder(chatModel)
                .defaultSystem(promptResource)
                .defaultAdvisors(new PromptChatMemoryAdvisor(chatMemory))
                .build();
    }

    @Bean
    @Qualifier("chatClients")
    public Map<AIModel, ChatClient> chatClients(
            @Qualifier("openAiChatClient") ChatClient openAiChatClient,
            @Qualifier("geminiChatClient") ChatClient geminiChatClient) {
        return Map.of(
                AIModel.OPENAI_MODEL, openAiChatClient,
                AIModel.GEMINI_MODEL, geminiChatClient
        );
    }

}
