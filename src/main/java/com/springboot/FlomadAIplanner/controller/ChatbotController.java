package com.springboot.FlomadAIplanner.controller;

import com.springboot.FlomadAIplanner.model.ChatMessage;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chatbot")
public class ChatbotController {

    @PostMapping("/message")
    public String handleMessage(@RequestBody ChatMessage message) {
        String userMessage = message.getText();
        return "You said: " + userMessage + ". How can I help you further?";
    }
}
