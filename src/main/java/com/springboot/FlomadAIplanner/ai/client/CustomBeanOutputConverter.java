package com.springboot.FlomadAIplanner.ai.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.springboot.FlomadAIplanner.ai.service.JsonSchemaService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.lang.NonNull;

@Slf4j
public class CustomBeanOutputConverter<T> extends BeanOutputConverter<T> {

    private final JsonSchemaService jsonSchemaService;

    public CustomBeanOutputConverter(Class<T> clazz) {
        super(clazz);
        jsonSchemaService = new JsonSchemaService(super.getObjectMapper());
    }

    public CustomBeanOutputConverter(Class<T> clazz, ObjectMapper objectMapper) {
        super(clazz, objectMapper);
        jsonSchemaService = new JsonSchemaService(objectMapper);
    }

    public CustomBeanOutputConverter(ParameterizedTypeReference<T> typeRef) {
        super(typeRef);
        jsonSchemaService = new JsonSchemaService(super.getObjectMapper());
    }

    public CustomBeanOutputConverter(ParameterizedTypeReference<T> typeRef, ObjectMapper objectMapper) {
        super(typeRef, objectMapper);
        jsonSchemaService = new JsonSchemaService(objectMapper);
    }

    @Override
    public T convert(@NonNull String text) {
        log.debug("📥 Raw AI response: {}", text);
        
        // First, apply existing JSON schema extraction
        String textToConvert = jsonSchemaService.extractPropertiesIfSchemaPresent(text);
        
        // Then, extract pure JSON by removing emojis and text before/after JSON
        textToConvert = extractPureJson(textToConvert);
        
        log.debug("🔄 Processed text for JSON parsing: {}", textToConvert);
        
        return super.convert(textToConvert); // Call the base class convert method with cleaned text
    }
    
    /**
     * Extracts pure JSON from text that may contain emojis, text, or other content before/after JSON.
     * Looks for the first opening curly brace { and the last closing curly brace }.
     */
    private String extractPureJson(String text) {
        if (text == null || text.trim().isEmpty()) {
            return text;
        }
        
        // Find the first opening curly brace
        int startIndex = text.indexOf('{');
        if (startIndex == -1) {
            log.warn("⚠️ No opening curly brace found in response: {}", text);
            return text; // Return as-is if no JSON structure found
        }
        
        // Find the last closing curly brace
        int endIndex = text.lastIndexOf('}');
        if (endIndex == -1 || endIndex < startIndex) {
            log.warn("⚠️ No valid closing curly brace found in response: {}", text);
            return text; // Return as-is if no valid JSON structure found
        }
        
        // Extract the JSON portion
        String jsonPortion = text.substring(startIndex, endIndex + 1);
        
        if (!jsonPortion.equals(text.trim())) {
            log.info("🧹 Stripped non-JSON content. Original length: {}, JSON length: {}", 
                    text.length(), jsonPortion.length());
        }
        
        return jsonPortion;
    }
}
