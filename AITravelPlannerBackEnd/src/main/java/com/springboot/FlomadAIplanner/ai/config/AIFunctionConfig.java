package com.springboot.FlomadAIplanner.ai.config;

import com.springboot.FlomadAIplanner.ai.functions.AIFunction;
import com.springboot.FlomadAIplanner.ai.util.FunctionAnnotationUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class AIFunctionConfig {

    private final List<AIFunction<?, ?>> aiFunctions;
    private final FunctionAnnotationUtils functionAnnotationUtils;

    @Autowired
    public AIFunctionConfig(List<AIFunction<?, ?>> aiFunctions, FunctionAnnotationUtils functionAnnotationUtils) {
        this.aiFunctions = aiFunctions;
        this.functionAnnotationUtils = functionAnnotationUtils;
    }

    @Bean
    public List<String> aiFunctionDescriptions() {
        return functionAnnotationUtils.extractBeanNamesAndDescriptions
                (aiFunctions.stream()
                        .map(AIFunction::getClass)
                        .toArray(Class[]::new));
    }

}
