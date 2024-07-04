package com.springboot.FlomadAIplanner.ai.agents;

import com.springboot.FlomadAIplanner.ai.agents.messageProcessing.MessageProcessingStrategy;
import com.springboot.FlomadAIplanner.ai.config.AIConfig;
import com.springboot.FlomadAIplanner.ai.dto.AIMessageDTO;
import com.springboot.FlomadAIplanner.ai.factory.ChatClientFactory;
import com.springboot.FlomadAIplanner.ai.agents.FunctionNameSuggestion;
import lombok.Getter;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;

public class Agent<T> {

    @Value("classpath:/templates/FunctionsSuggestionsPrompt.txt")
    protected Resource functionSuggestionsPrompt;
    protected final ChatClient chatClient;
    protected final Map<String, String> functionsUsable;
    protected final MessageProcessingStrategy<T> messageProcessingStrategy;
    protected final Resource prompt;

    public Agent(Builder<T> builder){
        this.chatClient = builder.chatClient;
        this.functionsUsable = builder.functionsUsable;
        this.messageProcessingStrategy = builder.messageProcessingStrategy;
        this.prompt = builder.prompt;
    }


    public Agent(
            ChatClientFactory chatClientFactory,
            Model model,
            Resource prompt,
            Map<String, String> functions,
            MessageProcessingStrategy<T> messageProcessingStrategy
    ){
        this.chatClient = chatClientFactory.getClient(model.getModelName());
        this.functionsUsable = functions;
        this.messageProcessingStrategy = messageProcessingStrategy;
        this.prompt = prompt;
    }

    public static class Builder<T> {
        private ChatClient chatClient;
        private Map<String, String> functionsUsable;
        private MessageProcessingStrategy<T> messageProcessingStrategy;
        private Resource prompt;

        public Builder() {}

        public Builder<T> setChatClient(ChatClient client){
            this.chatClient = client;
            return this;
        }

        public Builder<T> setFunctions(Map<String, String> functionsUsable) {
            this.functionsUsable = functionsUsable;
            return this;
        }

        public Builder<T> setMessageProcessingStrategy(MessageProcessingStrategy<T> strategy) {
            this.messageProcessingStrategy = strategy;
            return this;
        }

        public Builder<T> setPrompt(Resource prompt) {
            this.prompt = prompt;
            return this;
        }

        public Agent<T> build() {
            return new Agent<T>(this);
        }
    }

    public ChatClient getChatClient() {
        return this.chatClient;
    }

    public List<FunctionNameSuggestion> getRequiredFunctions(String message) {
        return chatClient.prompt()
                .user(u -> u.text(functionSuggestionsPrompt)
                        .param("functionsList", FunctionAnnotationUtils.mapToString(functionsUsable))
                        .param("message", message))
                .call()
                .entity(new ParameterizedTypeReference<>() {
                });
    }

    protected String[] functionNames(List<FunctionNameSuggestion> functions) {
        return functions.stream()
                .map(FunctionNameSuggestion::functionName)
                .toArray(String[]::new);
    }

    public String[] getFunctions(String message) {
        List<FunctionNameSuggestion> functions = getRequiredFunctions(message);
        return functionNames(functions);
    }

    public T processMessage(AIMessageDTO message) {
        return messageProcessingStrategy.processMessage(message, this, this.prompt);
    }

    public T processMessage(AIMessageDTO message, String prompt) {
        return messageProcessingStrategy.processMessage(message, this, prompt);
    }

}
