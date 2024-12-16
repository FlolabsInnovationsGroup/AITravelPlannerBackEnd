package com.springboot.FlomadAIplanner.ai.config;

import com.springboot.FlomadAIplanner.ai.util.FunctionAnnotationUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.function.Function;

@Configuration
public class AIFunctionConfig {

    private final List<Function<?, ?>> aiFunctions;
    private final FunctionAnnotationUtils functionAnnotationUtils;

    @Autowired
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

}
