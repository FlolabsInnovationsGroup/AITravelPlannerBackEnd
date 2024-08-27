package com.springboot.FlomadAIplanner.dto;

public class ChatRequest {
    private String model;
    private String messages;

    // Getters and setters
    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getMessages() {
        return messages;
    }

    public void setMessages(String messages) {
        this.messages = messages;
    }
}

