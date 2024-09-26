package com.springboot.FlomadAIplanner.ai.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import java.util.UUID;

@RestController
@RequestMapping("/api/guest-users")
public class GuestController {

    @PostMapping
    public ResponseEntity<String> createGuestSession(HttpSession session) {
        String sessionId = (String) session.getAttribute("guestSessionId");
        if (sessionId == null) {
            sessionId = UUID.randomUUID().toString();
            session.setAttribute("guestSessionId", sessionId);
        }
        return ResponseEntity.ok(sessionId);
    }
}