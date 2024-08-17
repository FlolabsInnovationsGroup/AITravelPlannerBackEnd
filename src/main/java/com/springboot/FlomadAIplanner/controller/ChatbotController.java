package com.springboot.FlomadAIplanner.controller;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chatbot")
public class ChatbotController {

    @PostMapping("/message")
    public String handleMessage(@RequestBody ChatMessage message) {
        String userMessage = message.getText();
        String response = "You said: " + userMessage + ". How can I help you further?";
        return response;
    }
}

class ChatMessage {
    private String text;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
