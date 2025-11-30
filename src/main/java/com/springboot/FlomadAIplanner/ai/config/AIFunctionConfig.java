package com.springboot.FlomadAIplanner.ai.config;

import com.springboot.FlomadAIplanner.ai.util.FunctionAnnotationUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.function.Function;

@Configuration
public class AIFunctionConfig {

    private final List<Function<?, ?>> aiFunctions;
    private final FunctionAnnotationUtils functionAnnotationUtils;

    public AIFunctionConfig(List<Function<?, ?>> aiFunctions, FunctionAnnotationUtils functionAnnotationUtils) {
        this.aiFunctions = aiFunctions;
        this.functionAnnotationUtils = functionAnnotationUtils;
    }

    @Bean
    public List<String> aiFunctionDescriptions() {
        return functionAnnotationUtils.extractBeanNamesAndDescriptions
                (aiFunctions.stream()
                        .map(Function::getClass)
                        .toArray(Class[]::new));
    }

    /**
     * Extracts ONLY bean names (without descriptions) for Spring AI function registration
     */
    @Bean(name = "aiFunctionBeanNames")
    public List<String> aiFunctionBeanNames() {
        return functionAnnotationUtils.extractBeanNames
                (aiFunctions.stream()
                        .map(Function::getClass)
                        .toArray(Class[]::new));
    }

}
