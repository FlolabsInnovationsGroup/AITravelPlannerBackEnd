package com.springboot.FlomadAIplanner.ai.config.cnst;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AIModel {
    OPENAI_MODEL("openai"),
    GEMINI_MODEL("gemini");

    private final String modelName;
}
