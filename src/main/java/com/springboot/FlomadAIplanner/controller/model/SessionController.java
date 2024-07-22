package com.springboot.FlomadAIplanner.controller.model;

import com.springboot.FlomadAIplanner.dto.SessionOperateDTO;
import com.springboot.FlomadAIplanner.dto.SessionDTO;
import com.springboot.FlomadAIplanner.dto.UpdateTitleRequest;
import com.springboot.FlomadAIplanner.model.entities.Message;
import com.springboot.FlomadAIplanner.model.entities.Session;
import com.springboot.FlomadAIplanner.service.model.MessageService;
import com.springboot.FlomadAIplanner.service.model.SessionService;
import com.springboot.FlomadAIplanner.util.JwtUtil;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    MessageService messageService;

    public SessionController(SessionService sessionService, JwtUtil jwtUtil) {
        this.sessionService = sessionService;
        this.jwtUtil = jwtUtil;
    }

    /**
     * @description get all the id of all sessions under a given user
     */
    @GetMapping("/sessions")
    public List<SessionDTO> getSessionIdsByUserId(HttpServletRequest request) {
        final String authorizationHeader = request.getHeader("Authorization");
        String username = null;
        String jwt = null;
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwt = authorizationHeader.substring(7);
            username = jwtUtil.extractUsername(jwt);
        }
        log.info("Retrieving session ids for user: {}", username);
        return sessionService.findSessionIdsByUserId(username);
    }

    /**
     * @description get all the messages of all sessions under a given user
     */
    @GetMapping("/messages")
    public List<Message> getMessagesByUserId(HttpServletRequest request) {
        log.info("request:{}",request);
        final String authorizationHeader = request.getHeader("Authorization");
        log.info("AuthorizationHeader:{}",authorizationHeader);
        String username = null;
        String jwt = null;
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwt = authorizationHeader.substring(7);
            username = jwtUtil.extractUsername(jwt);
        }
        
        log.info("Retrieving messages for user: {}", username);
        return messageService.getMessagesByUsername(username);
    }

    /**
     * @description add a new line in the session table
     * ,@RequestHeader("Authorization") String token
     */
    @PostMapping("/add_session")
    public ResponseEntity<Session> addSession(@RequestBody SessionOperateDTO sessionDTO,HttpServletRequest request) {
        final String authorizationHeader = request.getHeader("Authorization");
        String username = null;
        String jwt = null;
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwt = authorizationHeader.substring(7);
            username = jwtUtil.extractUsername(jwt);
        }
        if (sessionDTO.getSessionId() == null || sessionDTO.getSessionId().isEmpty()) {
            String newId;
            do {
                newId = UUID.randomUUID().toString();
            } while (sessionService.existsById(newId));
            sessionDTO.setSessionId(newId);
            log.info("Generated new Session ID: {}", sessionDTO.getSessionId());
            // throw new RuntimeException("Session ID is not provided.");
        }
        log.info("SessionId:{}",sessionDTO.getSessionId());
        String userId = jwtUtil.extractUsername(jwt);
        Session session = new Session();
        session.setUserId(userId);
        session.setTitle(sessionDTO.getTitle());
        session.setSessionId(sessionDTO.getSessionId());
        session.setCreatedAt(sessionDTO.getCreatedAt());
        session.setUpdatedAt(sessionDTO.getUpdatedAt());

        log.info("Creating new session: {}", session);
        Session savedSession = sessionService.saveSession(session);
        log.info("Session created");
        return ResponseEntity.ok(savedSession);
    }

    @PostMapping("/sessions/{sessionId}")
    public ResponseEntity<Session> updateSession(HttpServletRequest request, @PathVariable String sessionId,
                                                 @RequestBody SessionOperateDTO sessionOperateDTO) {
        final String authorizationHeader = request.getHeader("Authorization");
        String username = null;
        String jwt = null;
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwt = authorizationHeader.substring(7);
            username = jwtUtil.extractUsername(jwt);
        }
        if (!this.sessionService.existsByUserId(username)) {
            throw new RuntimeException("User not found");
        }
        if (!this.sessionService.existsBySessionIdAndUserId(sessionId, username)) {
            throw new RuntimeException("Session not found for the user");
        }
        Session updatedSession = sessionService.updateSession(sessionId, username, sessionOperateDTO.getTitle(), sessionOperateDTO.getUpdatedAt());
        return ResponseEntity.ok(updatedSession);
    }

    @PostMapping("sessions/{sessionId}/updateTitle")
    public ResponseEntity<Session> updateSessionTitle(HttpServletRequest request, @PathVariable String sessionId,
                                                      @RequestBody UpdateTitleRequest updateRequest) {
        try {
            final String authorizationHeader = request.getHeader("Authorization");
            String username = null;
            String jwt = null;
            if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
                jwt = authorizationHeader.substring(7);
                username = jwtUtil.extractUsername(jwt);
            }
            if (!this.sessionService.existsByUserId(username)) {
                throw new RuntimeException("User not found");
            }
            if (!this.sessionService.existsBySessionIdAndUserId(sessionId, username)) {
                throw new RuntimeException("Session not found for the user");
            }
            Session updatedSession = sessionService.updateSessionTitle(sessionId, username, updateRequest.getTitle());
            return ResponseEntity.ok(updatedSession);
        } catch (RuntimeException e) {
            log.error("Error updating session title: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }
}

