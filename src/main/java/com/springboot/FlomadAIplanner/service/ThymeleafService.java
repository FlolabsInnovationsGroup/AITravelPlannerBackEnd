package com.springboot.FlomadAIplanner.service;
import lombok.extern.slf4j.Slf4j;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ThymeleafService {
    private final TemplateEngine templateEngine;

    @Autowired
    public ThymeleafService(TemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }

    public String renderPlainText(String templateName, Map<String, Object> variables) {
        log.debug("Rendering template: {}", templateName);
        Context context = new Context();
        context.setVariables(variables);
        return templateEngine.process(templateName, context);
    }
}
