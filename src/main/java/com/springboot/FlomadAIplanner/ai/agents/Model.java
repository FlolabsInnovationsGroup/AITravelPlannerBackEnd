package com.springboot.FlomadAIplanner.ai.agents;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Model {
    OPENAI_MODEL("openai"),
    GEMINI_MODEL("gemini");

    private final String modelName;
}
