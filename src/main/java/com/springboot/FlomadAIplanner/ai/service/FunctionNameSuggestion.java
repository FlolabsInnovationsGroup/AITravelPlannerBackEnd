package com.springboot.FlomadAIplanner.ai.service;

import lombok.AllArgsConstructor;
import lombok.Data;
/**
 * Represents a suggestion for a function or action.
 */
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
