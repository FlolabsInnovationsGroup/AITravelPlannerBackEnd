package com.springboot.FlomadAIplanner.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.springboot.FlomadAIplanner.config.GlobalConstants;

import javax.annotation.PostConstruct;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ModelController {

    private String selectedModel;

    // TODO: store the settings in the database.

    @PostConstruct
    public void init() {
        this.selectedModel = GlobalConstants.DEFAULT_LLM_MODEL;
    }

    @PostMapping("/set-model")
    public ResponseEntity<Map<String, String>> setModel(@RequestBody Map<String, String> request) {
        this.selectedModel = request.get("model");
        Map<String, String> response = new HashMap<>();
        response.put("message", "Model set to " + this.selectedModel);
        return ResponseEntity.ok(response);
    }

    public String getSelectedModel() {
        return selectedModel;
    }
}
