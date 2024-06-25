package com.springboot.FlomadAIplanner.controller;

import com.springboot.FlomadAIplanner.config.GlobalConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

@Slf4j
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
        log.info("Model is setting to {}", this.selectedModel);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Model set to " + this.selectedModel);
        log.info("Model set to {}", this.selectedModel);
        return ResponseEntity.ok(response);
    }

    public String getSelectedModel() {
        return selectedModel;
    }
}
