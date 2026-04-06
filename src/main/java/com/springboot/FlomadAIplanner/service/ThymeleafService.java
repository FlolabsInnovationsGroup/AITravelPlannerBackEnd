package com.springboot.FlomadAIplanner.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Map;

@Slf4j
@Component
public class ThymeleafService {

    /** Thymeleaf template engine. */
    private final TemplateEngine templateEngine;

    /**
     * Constructor.
     *
     * @param pTemplateEngine Thymeleaf template engine
     */
    public ThymeleafService(
            @Qualifier("customTemplateEngine") final TemplateEngine pTemplateEngine) {
        this.templateEngine = pTemplateEngine;
    }

    /**
     * Renders a Thymeleaf template to plain text.
     *
     * @param templateName template name
     * @param variables template variables
     * @return rendered string
     */
    public String renderPlainText(final String templateName,
            final Map<String, Object> variables) {
        log.debug("Rendering template: {}", templateName);
        Context context = new Context();
        context.setVariables(variables);
        return templateEngine.process(templateName, context);
    }
}
