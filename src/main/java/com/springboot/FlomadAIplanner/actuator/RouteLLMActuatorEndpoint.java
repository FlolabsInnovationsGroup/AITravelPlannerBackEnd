package com.springboot.FlomadAIplanner.actuator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.stereotype.Component;

import com.springboot.FlomadAIplanner.service.RouteLLMService;

@Component
@Endpoint(id = "test-route-llm")
public class RouteLLMActuatorEndpoint {

    @Autowired
    private RouteLLMService routeLLMService;

    @ReadOperation
    public String testRouteLLM() {
        return routeLLMService.createChatCompletion("gpt-3.5-turbo", "Hello");
    }
}