package com.springboot.FlomadAIplanner.controller.DataPersistance;

import com.springboot.FlomadAIplanner.DTO.SessionDto;
import com.springboot.FlomadAIplanner.DTO.UpdateTitleRequest;
import com.springboot.FlomadAIplanner.model.entities.Session;
import com.springboot.FlomadAIplanner.service.DataPersistance.SessionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


/**
 * @class
 * @description provide a controller for the front end that can operate the database
 */
@Slf4j
@RestController
public class SessionController {
    private final SessionService sessionService;

    public SessionController(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    /**
     * @method
     * @description get all the id of all sessions under a given user
     */
    @GetMapping("/users/{userId}/sessions")
    public List<SessionDto> getSessionIdsByUserId(@PathVariable String userId) {
        log.info("Retrieving session ids for user: {}", userId);
        return sessionService.findSessionIdsByUserId(userId);
    }

    /**
     * @method
     * @description add a new line in the session table
     */
    @PostMapping("/add_session")
    public ResponseEntity<Session> addSession(@RequestBody Session session) {
        log.info("Creating new session: {}", session);
        if (session.getSessionId() == null || session.getSessionId().isEmpty()) {
            log.error("Session ID is not provided.");
            throw new RuntimeException("Session ID is not provided.");
        }
        Session savedSession = sessionService.saveSession(session);
        log.info("Session created");
        return ResponseEntity.ok(savedSession);
    }

    @PostMapping("/users/{userId}/sessions/{sessionId}")
    public ResponseEntity<Session> updateSession(@PathVariable String userId, @PathVariable String sessionId,
                                                 @RequestBody SessionDto sessionDto) {
        Session updatedSession = sessionService.updateSession(sessionId, userId, sessionDto.getTitle(), sessionDto.getTimestamp());
        return ResponseEntity.ok(updatedSession);
    }

    @PostMapping("/users/{userId}/sessions/{sessionId}/updateTitle")
    public ResponseEntity<Session> updateSessionTitle(@PathVariable String userId, @PathVariable String sessionId,
                                                      @RequestBody UpdateTitleRequest updateRequest) {
        try {
            Session updatedSession = sessionService.updateSessionTitle(sessionId, userId, updateRequest.getTitle());
            return ResponseEntity.ok(updatedSession);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

}

