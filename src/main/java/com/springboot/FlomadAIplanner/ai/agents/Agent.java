package com.springboot.FlomadAIplanner.ai.agents;

import com.springboot.FlomadAIplanner.ai.agents.messageProcessing.MessageProcessingStrategy;
import com.springboot.FlomadAIplanner.ai.dto.AIMessageDTO;
import com.springboot.FlomadAIplanner.ai.factory.ChatClientFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.Resource;

import java.util.List;
import java.util.Map;

@Slf4j
public class Agent<T> {

    @Value("classpath:/templates/FunctionsSuggestionsPrompt.txt")
    protected Resource functionSuggestionsPrompt;
    protected final ChatClient chatClient;
    protected final List<String> functionsUsable;
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
            List<String> functions,
            MessageProcessingStrategy<T> messageProcessingStrategy
    ){
        this.chatClient = chatClientFactory.getClient(model.getModelName());
        this.functionsUsable = functions;
        this.messageProcessingStrategy = messageProcessingStrategy;
        this.prompt = prompt;
    }

    public static class Builder<T> {
        private ChatClient chatClient;
        private List<String> functionsUsable;
        private MessageProcessingStrategy<T> messageProcessingStrategy;
        private Resource prompt;

        public Builder() {}

        public Builder<T> setChatClient(ChatClient client){
            this.chatClient = client;
            return this;
        }

        public Builder<T> setFunctions(List<String> functionsUsable) {
            log.info("Usable Functions: {}", functionsUsable);
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
                .user(u -> {
                    log.info("Creating user prompt with params: Prompt={}, functionsList={}, message={}",
                            functionSuggestionsPrompt, functionsUsable.toString(), message);
                    u.text(functionSuggestionsPrompt)
                            .param("functionsList", functionsUsable.toString())
                            .param("message", message);
                })
                .call()
                .entity(new ParameterizedTypeReference<>() {
                });
    }

    protected String[] functionNames(List<FunctionNameSuggestion> functions) {
        log.info("functions: {}", functions);
        return functions.stream()
                .map(FunctionNameSuggestion::functionName)
                .toArray(String[]::new);
    }

    public String[] getFunctions(String message) {
        List<FunctionNameSuggestion> functions = getRequiredFunctions(message);
        return functionNames(functions);
    }

    public String[] getFunctions(List<FunctionNameSuggestion> functions) {
        return functionNames(functions);
    }

    public T processMessage(AIMessageDTO message) {
        PromptTemplate promptTemplate = new PromptTemplate(this.prompt);
        return messageProcessingStrategy.processMessage(message, this, promptTemplate);
    }

    public T processMessage(AIMessageDTO message, String prompt) {
        PromptTemplate promptTemplate = new PromptTemplate(prompt);
        return messageProcessingStrategy.processMessage(message, this, promptTemplate);
    }


}
