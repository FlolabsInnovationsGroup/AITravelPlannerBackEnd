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
        String textToConvert = jsonSchemaService.extractPropertiesIfSchemaPresent(text);
        return super.convert(textToConvert); // Call the base class convert method with potentially modified text
    }
}
