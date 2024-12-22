// File: JsonSchemaService.java
package com.springboot.FlomadAIplanner.ai.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class JsonSchemaService {

    private final ObjectMapper objectMapper;

    /**
     * Extracts the 'properties' node from the JSON text if the '$schema' key is present.
     *
     * @param text The input JSON string.
     * @return The 'properties' node as a JSON string if '$schema' is present; otherwise, returns the original text.
     */
    public String extractPropertiesIfSchemaPresent(String text) {
        try {
            log.info("Processing JSON text: {}", StringUtils.substring(text, 0, 1000));
            JsonNode rootNode = objectMapper.readTree(text);

            if (rootNode.has("$schema")) {
                log.info("Found '$schema' key in JSON, using 'properties' node");
                JsonNode propertiesNode = rootNode.get("properties");
                if (propertiesNode != null) {
                    return propertiesNode.toString();
                }
            }
            return text;
        } catch (Exception e) {
            log.error("Failed to parse and retrieve 'properties' key", e);
            throw new RuntimeException("Failed to parse and manipulate JSON", e);
        }
    }
}
