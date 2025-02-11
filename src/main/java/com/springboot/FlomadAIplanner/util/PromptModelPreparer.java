package com.springboot.FlomadAIplanner.util;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Component
public class PromptModelPreparer {

    @Autowired
    private HttpServletRequest request;

    public Map<String, Object> prepareModel(String templateString) {
        Map<String, Object> model = new HashMap<>();

        // Add user-specific data
        String username = request.getUserPrincipal() != null
                ? request.getUserPrincipal().getName()
                : "Guest";
        model.put("username", username);

        // Add request-specific data
        String destination = request.getParameter("destination");
        model.put("destination", destination != null ? destination : "Unknown Destination");

        // Validate against the template
        validateTemplateTokens(templateString, model);

        return model;
    }

    private void validateTemplateTokens(String templateString, Map<String, Object> model) {
        PromptTemplate promptTemplate = new PromptTemplate(templateString);
        Set<String> requiredTokens = promptTemplate.getInputVariables();

        for (String token : requiredTokens) {
            if (!model.containsKey(token)) {
                throw new IllegalStateException("Missing value for template token: " + token);
            }
        }
    }
}
