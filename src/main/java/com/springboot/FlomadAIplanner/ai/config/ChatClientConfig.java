package com.springboot.FlomadAIplanner.ai.config;

import com.springboot.FlomadAIplanner.ai.config.cnst.AIModel;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.PromptChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.Resource;

import java.util.List;
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
    public ChatClient openAiChatClient(ChatModel chatModel,
                                       @Qualifier("customDatabaseChatMemoryFixed") ChatMemory chatMemory) {
        // GPT-4o compatible options
        OpenAiChatOptions options = OpenAiChatOptions.builder()
                .model("gpt-4o")
                .maxCompletionTokens(3000)
                .temperature(0.7)
                .build();
        
        return ChatClient.builder(chatModel)
                .defaultOptions(options)
                .defaultSystem(promptResource)
                .defaultAdvisors(new PromptChatMemoryAdvisor(chatMemory))
                .build();
    }

    @Bean
    @Primary
    public ChatModel primaryChatModel(ChatModel chatModel) {
        // Use Spring AI auto-configured ChatModel but override with GPT-5 compatible options
        return chatModel;
    }

    // Vertex AI Gemini temporarily disabled to resolve bean conflicts
    /*
    @Bean
    public ChatClient geminiChatClient(@Qualifier("vertexAiGeminiChat") ChatModel chatModel,
                                       @Qualifier("customDatabaseChatMemory") ChatMemory chatMemory) {
        return ChatClient.builder(chatModel)
                .defaultSystem(promptResource)
                .defaultAdvisors(new PromptChatMemoryAdvisor(chatMemory))
                .build();
    }
    */

    @Bean
    @Qualifier("chatClients")
    public Map<AIModel, ChatClient> chatClients(
            @Qualifier("openAiChatClient") ChatClient openAiChatClient) {
        return Map.of(
                AIModel.OPENAI_MODEL, openAiChatClient
                // AIModel.GEMINI_MODEL, geminiChatClient // Disabled temporarily
        );
    }

}
