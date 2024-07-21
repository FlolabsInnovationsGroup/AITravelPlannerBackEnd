package com.springboot.FlomadAIplanner.controller.model;

import com.springboot.FlomadAIplanner.dto.AddSessionDTO;
import com.springboot.FlomadAIplanner.dto.SessionDTO;
import com.springboot.FlomadAIplanner.dto.UpdateTitleRequest;
import com.springboot.FlomadAIplanner.model.entities.Session;
import com.springboot.FlomadAIplanner.service.model.SessionService;
import com.springboot.FlomadAIplanner.util.JwtUtil;

import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;


/**
 * @class
 * @description provide a controller for the front end that can operate the database
 */
@Slf4j
@RestController
public class SessionController {
    private final SessionService sessionService;
    private JwtUtil jwtUtil;

    public SessionController(SessionService sessionService, JwtUtil jwtUtil) {
        this.sessionService = sessionService;
        this.jwtUtil = jwtUtil;
    }

    /**
     * @description get all the id of all sessions under a given user
     */
    @GetMapping("/users/{userId}/sessions")
    public List<SessionDTO> getSessionIdsByUserId(@PathVariable String userId) {
        log.info("Retrieving session ids for user: {}", userId);
        return sessionService.findSessionIdsByUserId(userId);
    }

    /**
     * @description add a new line in the session table
     */
    @PostMapping("/add_session")
    public ResponseEntity<Session> addSession(@RequestBody AddSessionDTO sessionDTO,@RequestHeader("Authorization") String token) {
        if (sessionDTO.getSessionId() == null || sessionDTO.getSessionId().isEmpty()) {
            String newId;
            do {
                newId = UUID.randomUUID().toString();
            } while (sessionService.existsById(newId));
            sessionDTO.setSessionId(newId);
            log.info("Generated new Session ID: {}", sessionDTO.getSessionId());
            // throw new RuntimeException("Session ID is not provided.");
        }
        String userId = jwtUtil.extractUsername(token);
        Session session = new Session();
        session.setUserId(userId);
        session.setTitle(sessionDTO.getTitle());
        session.setCreatedAt(sessionDTO.getCreatedAt());
        session.setUpdatedAt(sessionDTO.getUpdatedAt());

        log.info("Creating new session: {}", session);
        Session savedSession = sessionService.saveSession(session);
        log.info("Session created");
        return ResponseEntity.ok(savedSession);
    }

    @PostMapping("/users/{userId}/sessions/{sessionId}")
    public ResponseEntity<Session> updateSession(@PathVariable String userId, @PathVariable String sessionId,
                                                 @RequestBody SessionDTO sessionDto) {
        Session updatedSession = sessionService.updateSession(sessionId, userId, sessionDto.getTitle(), sessionDto.getTimestamp());
        return ResponseEntity.ok(updatedSession);
    }

    @PostMapping("/users/{userId}/sessions/{sessionId}/updateTitle")
    public ResponseEntity<Session> updateSessionTitle(@PathVariable String userId, @PathVariable String sessionId,
                                                      @RequestBody UpdateTitleRequest updateRequest) {
        try {
            if (!this.sessionService.existsByUserId(userId)) {
                throw new RuntimeException("User not found");
            }
            if (!this.sessionService.existsBySessionIdAndUserId(sessionId, userId)) {
                throw new RuntimeException("Session not found for the user");
            }
            Session updatedSession = sessionService.updateSessionTitle(sessionId, userId, updateRequest.getTitle());
            return ResponseEntity.ok(updatedSession);
        } catch (RuntimeException e) {
            log.error("Error updating session title: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }
}

