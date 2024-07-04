package com.springboot.FlomadAIplanner.ai.agents;

import org.codehaus.groovy.util.ListHashMap;
import org.springframework.context.annotation.Description;

import java.util.*;

public class FunctionAnnotationUtils {

    /**
     * For the provided classes, extract their name and descriptions and store them in a List of String
     * @param classes Provided Classes
     * @return List of String of Class Names and Descriptions
     */
    public static List<String> extract(Class<?>... classes) {

        List<String> classDescriptions = new ArrayList<>();

        // Iterate through the list and store the class name and annotation value
        for (Class<?> clazz : classes) {
            // Get the @Description annotation
            Description description = clazz.getAnnotation(Description.class);

            if (description != null) {
                // Store the class name and description value in the map
                classDescriptions.add(
                        String.format("Name: %s, Description: %s", toCamelCase(clazz.getSimpleName()), description.value())
                );
            }

        }

        return classDescriptions;
    }

    /**
     * Convert the first Letter of a String to Lower Case
     * @param str Input String
     * @return Converted String
     */
    public static String toCamelCase(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toLowerCase() + str.substring(1);
    }


    /**
     * Convert a Map to String
     * @param map
     * @return String
     */
    public static String mapToString(Map<String, ?> map) {
        StringBuilder stringBuilder = new StringBuilder();

        for (Map.Entry<String, ?> entry : map.entrySet()) {
            stringBuilder.append("Class: ")
                    .append(entry.getKey())
                    .append(entry.getValue() != null ? entry.getValue().toString() : "null")  // Handle Object to String conversion
                    .append("\n");
        }

        return stringBuilder.toString();
    }

    public static String listMapToString(List<Map<String, ?>> list) {
        StringBuilder stringBuilder = new StringBuilder();

        for (Map<String, ?> map : list) {
            stringBuilder.append(mapToString(map));
        }

        return stringBuilder.toString();
    }


}