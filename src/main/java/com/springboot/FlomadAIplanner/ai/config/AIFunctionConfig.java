package com.springboot.FlomadAIplanner.ai.config;

import com.springboot.FlomadAIplanner.ai.functions.AIFunction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Description;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Configuration
public class AIFunctionConfig {

    private final ApplicationContext applicationContext;
    private final List<AIFunction<?, ?>> aiFunctions;

    @Autowired
    public AIFunctionConfig(ApplicationContext applicationContext, List<AIFunction<?, ?>> aiFunctions) {
        this.applicationContext = applicationContext;
        this.aiFunctions = aiFunctions;
    }

    @Bean
    public List<String> aiFunctionDescriptions() {
        Map<Class<?>, String> classToBeanNameMap = mapClassToBeanName();

        return aiFunctions.stream()
                .map(aiFunction -> {
                    String beanName = classToBeanNameMap.get(aiFunction.getClass());
                    String descriptionText = getDescriptionText(aiFunction.getClass());
                    return String.format("Bean name: %s, Description: %s",
                            beanName != null ? beanName : "Unnamed Bean",
                            descriptionText);
                })
                .collect(Collectors.toList());
    }

    private Map<Class<?>, String> mapClassToBeanName() {
        return aiFunctions.stream()
                .collect(Collectors.toMap(
                        AIFunction::getClass,
                        function -> applicationContext.getBeanNamesForType(function.getClass())[0],
                        (existing, replacement) -> existing // In case of conflicts, keep the existing mapping
                ));
    }


    private String getDescriptionText(Class<?> clazz) {
        Description description = clazz.getAnnotation(Description.class);
        if (description != null) {
            return description.value();
        } else {
            return "No description";
        }
    }

}
