package com.springboot.FlomadAIplanner.controller;

import com.springboot.FlomadAIplanner.service.socket.WebSocketService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
public class TestController {

    private final WebSocketService webSocketService;

    public TestController(WebSocketService webSocketService) {
        this.webSocketService = webSocketService;
    }

    @PostMapping("/clear-map")
    public ResponseEntity<?> clearMap(@RequestParam String sessionId) {
        webSocketService.sendMapCleared(sessionId);
        return ResponseEntity.ok().build();
    }
}
