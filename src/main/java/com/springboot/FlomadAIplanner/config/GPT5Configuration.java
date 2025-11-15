package com.springboot.FlomadAIplanner.config;

import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for GPT-5 model to ensure compatibility with OpenAI API restrictions.
 * GPT-5 only supports default values for temperature, top-p, frequency-penalty, and presence-penalty.
 */
@Configuration
public class GPT5Configuration {

    @Value("${spring.ai.openai.api-key}")
    private String apiKey;

    @Value("${spring.ai.openai.base-url:https://api.openai.com}")
    private String baseUrl;

    @Bean
    @Qualifier("openAiChatModel")
    public OpenAiChatModel openAiChatModel() {
        // Create OpenAI API client
        OpenAiApi openAiApi = new OpenAiApi(baseUrl, apiKey);
        
        // Create ChatModel without any temperature, top-p, etc. configurations
        // This ensures GPT-5 uses only default values which are supported
        return new OpenAiChatModel(openAiApi);
    }
}