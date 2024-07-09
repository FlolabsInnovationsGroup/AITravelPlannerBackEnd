package com.springboot.FlomadAIplanner.ai.service;

import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import java.util.List;
/**
 * ChatClientSuggestedProperties represents suggested properties for a chat client.
 * It includes multiple function names and a system prompt description.
 */
@Slf4j
@Component
@Description("Represents a suggestion for a function name, including the function's name and description.")
public class ChatClientSuggestedProperties {
    private List<String> functionNames;  // could be several
    private String systemPrompt; // should be only one

    public ChatClientSuggestedProperties() {
    }
    
    public ChatClientSuggestedProperties(List<String> functionNames, String systemPrompt) {
        this.systemPrompt = systemPrompt;
        this.functionNames = functionNames;
    }

    public List<String> getFunctionNames() {
        return functionNames;
    }

    public void setFunctionNames(List<String> functionNames) {
        this.functionNames = functionNames;
    }

    public String getSystemPrompt() {
        return systemPrompt;
    }

    public void setSystemPrompt(String systemPrompt) {
        this.systemPrompt = systemPrompt;
    }

}
