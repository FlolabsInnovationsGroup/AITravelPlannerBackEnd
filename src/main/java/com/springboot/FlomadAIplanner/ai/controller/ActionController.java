package com.springboot.FlomadAIplanner.ai.controller;

import com.springboot.FlomadAIplanner.ai.service.ActionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Slf4j
@Controller
@RequestMapping("/api/chat")
public class ActionController {

    @Autowired
    private ActionService actionService;

    @PostMapping("/action")
    public ResponseEntity<String> planTravel(@RequestBody List<String> messages) {
        log.info("Processing messages: {}", messages);

        String result = actionService.processMessages(messages);
        log.info("Received result from ActionService: {}", result);
        if (result.equals("No messages provided")) {
            return ResponseEntity.badRequest().body(result);
        }
        return ResponseEntity.ok(result);
    }
}