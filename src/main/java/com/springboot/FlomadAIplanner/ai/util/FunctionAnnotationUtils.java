package com.springboot.FlomadAIplanner.ai.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
public class FunctionAnnotationUtils {

    private final ApplicationContext applicationContext;

    public FunctionAnnotationUtils(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    /**
     * Extracts bean names and descriptions from the given list of AI functions.
     */
    public List<String> extractBeanNamesAndDescriptions(Class<?>... aiFunctions) {
        List<Class<?>> aiFunctionList = Arrays.asList(aiFunctions);
        Map<Class<?>, String> classToBeanNameMap = mapClassToBeanName(aiFunctionList);

        return Arrays.stream(aiFunctions)
                .map(aiFunction -> {
                    String beanName = classToBeanNameMap.get(aiFunction);
                    String descriptionText = getDescriptionText(aiFunction);
                    return String.format("Bean name: %s, Description: %s",
                            beanName != null ? beanName : "Unnamed Bean",
                            descriptionText);
                })
                .collect(Collectors.toList());
    }

    private Map<Class<?>, String> mapClassToBeanName(List<Class<?>> aiFunctions) {
        return aiFunctions.stream()
                .collect(Collectors.toMap(
                        functionClass -> functionClass,
                        functionClass -> applicationContext.getBeanNamesForType(functionClass)[0],
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
