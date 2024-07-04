package com.springboot.FlomadAIplanner.ai.agents;

import org.springframework.context.annotation.Description;

import java.util.*;

public class FunctionAnnotationUtils {
    public static Map<String, String> extract(Class<?>... classes) {

        Map<String, String> classDescriptions = new HashMap<>();

        // Iterate through the list and store the class name and annotation value
        for (Class<?> clazz : classes) {
            // Get the @Description annotation
            Description description = clazz.getAnnotation(Description.class);
            if (description != null) {
                // Store the class name and description value in the map
                classDescriptions.put(clazz.getSimpleName(), description.value());
            }
        }

        return classDescriptions;
    }

    public static String mapToString(Map<String, String> map) {
        StringBuilder stringBuilder = new StringBuilder();

        for (Map.Entry<String, String> entry : map.entrySet()) {
            stringBuilder.append("Class: ")
                    .append(entry.getKey())
                    .append(", Description: ")
                    .append(entry.getValue())
                    .append("\n");
        }

        return stringBuilder.toString();
    }
}