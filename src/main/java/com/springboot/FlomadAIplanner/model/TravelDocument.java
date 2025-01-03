package com.springboot.FlomadAIplanner.model;

import lombok.Getter;

import java.util.List;

public class TravelDocument {
    @Getter
    private String id;
    @Getter
    private String title;
    @Getter
    private String content;
    private final List<String> tags;

    public TravelDocument(String id, List<String> tags) {
        this.id = id;
        this.tags = tags;
    }

    public CharSequence getTags() {
        return (CharSequence) tags;
    }
    // Getters and Setters
}