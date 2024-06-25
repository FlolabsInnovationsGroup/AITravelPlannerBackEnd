package com.springboot.FlomadAIplanner.service.agentServices;

import com.springboot.FlomadAIplanner.service.GeminiService;
import com.springboot.FlomadAIplanner.service.ThymeleafService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class PlanningService {
    @Autowired
    private GeminiService geminiService;

    @Autowired
    private ThymeleafService thymeleafService;

    public String processIntent(List<String> messages) {
        log.info("Processing messages: {}", messages);
        if (messages == null || messages.isEmpty()) {
            log.error("No messages provided");
            return "No messages provided";
        }

        Map<String, Object> variables = new HashMap<>();
        variables.put("sessionMessages", String.join(" ", messages));
        variables.put("lastUserMessage", messages.get(messages.size() - 1));

        String geminiRequestText = thymeleafService.renderPlainText("IntentPrompt", variables);
        log.info("Text rendered successfully. Sending to Gemini.");
        return geminiService.sendMessageToGemini(geminiRequestText);
    }
}
