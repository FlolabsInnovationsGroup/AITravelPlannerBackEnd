package com.springboot.FlomadAIplanner.ai.service;

import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
/**
 * FunctionNameSuggestion represents a suggestion for a function name.
 * It includes the name and description of the function.
 */
@Slf4j
@Component
@Description("Represents a suggestion for a function name, including the function's name and description.")
public class FunctionNameSuggestion {
    private String functionNames;  
    private String description;

    public FunctionNameSuggestion(String functionNames, String description) {
        this.functionNames = functionNames;
        this.description = description;
    }

    public String getFunctionNames() {
        return functionNames;
    }

    public void setFunctionNames(String functionNames) {
        this.functionNames = functionNames;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "NameSuggestionFunction{" +
                "functionNames='" + functionNames + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
