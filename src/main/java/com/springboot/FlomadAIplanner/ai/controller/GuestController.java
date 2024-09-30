package com.springboot.FlomadAIplanner.ai.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/guest-users")
public class GuestController {

    @PostMapping
    public ResponseEntity<String> createGuestSession(String sessionId) {
        if (sessionId == null) {
            sessionId = UUID.randomUUID().toString();
        }
        return ResponseEntity.ok(sessionId);
    }
}