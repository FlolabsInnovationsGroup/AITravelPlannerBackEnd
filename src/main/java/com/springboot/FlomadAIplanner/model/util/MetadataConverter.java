package com.springboot.FlomadAIplanner.model.util;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Converter
public class MetadataConverter implements AttributeConverter<Map<String, Object>, String> {

    @Override
    public String convertToDatabaseColumn(Map<String, Object> metadata) {
        log.debug("Converting map to JSON: {}", metadata);
        return metadata.keySet().stream()
                .map(key -> key + "=" + metadata.get(key))
                .collect(Collectors.joining(", ", "{", "}"));
    }

    @Override
    public Map<String, Object> convertToEntityAttribute(String dbData) {
        log.debug("Converting JSON to map: {}", dbData);
        try {
            return Arrays.stream(dbData.split(","))
                    .map(entry -> entry.split("="))
                    .collect(Collectors.toMap(entry -> entry[0], entry -> entry[1]));
        } catch (Exception e) {
            log.error("Error converting JSON to map", e);
            throw new IllegalArgumentException("Error converting JSON to map", e);
        }
    }

}
