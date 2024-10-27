package com.springboot.FlomadAIplanner.controller.model;

import com.springboot.FlomadAIplanner.converter.Converter;
import com.springboot.FlomadAIplanner.dto.SessionDTO;
import com.springboot.FlomadAIplanner.model.entities.Session;
import com.springboot.FlomadAIplanner.service.model.SessionService;
import com.springboot.FlomadAIplanner.util.CurrentUserUtil;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/sessions")
public class SessionController implements CrudController<SessionDTO, String> {
    private final SessionService sessionService;
    private final Converter<SessionDTO, Session> sessionRevertConverter;
    private final Converter<Session, SessionDTO> sessionToDTOConverter;

    public SessionController(SessionService sessionService,
                             Converter<SessionDTO, Session> sessionRevertConverter,
                             Converter<Session, SessionDTO> sessionToDTOConverter) {
        this.sessionService = sessionService;
        this.sessionRevertConverter = sessionRevertConverter;
        this.sessionToDTOConverter = sessionToDTOConverter;
    }

    @Override
    @PostMapping
    public ResponseEntity<SessionDTO> create(@RequestBody SessionDTO sessionDTO) {
        Session session = sessionRevertConverter.convert(sessionDTO);
        log.info("Creating new session: {}", session);
        Session savedSession = sessionService.save(session);
        SessionDTO savedSessionDTO = sessionToDTOConverter.convert(savedSession);
        log.info("Session created");
        return ResponseEntity.ok(savedSessionDTO);
    }

    @Override
    @GetMapping("/{id}")
    public ResponseEntity<SessionDTO> getById(@PathVariable String id) {
        log.info("Retrieving session by id: {}", id);
        Session session = sessionService.findById(id);
        SessionDTO dto = sessionToDTOConverter.convert(session);
        return ResponseEntity.ok(dto);
    }

    @Override
    @GetMapping
    public ResponseEntity<List<SessionDTO>> getAll() {
        log.info("Retrieving all sessions");
        List<Session> sessions = sessionService.findAll();
        List<SessionDTO> dtos = sessions.stream()
                .map(sessionToDTOConverter::convert)
                .toList();
        return ResponseEntity.ok(dtos);
    }

    @Override
    @PutMapping("/{id}")
    public ResponseEntity<SessionDTO> update(@PathVariable String id, @RequestBody SessionDTO updatedEntity) {
        log.info("Updating session with id: {}", id);
        updatedEntity.setSessionId(id);
        Session session = sessionRevertConverter.convert(updatedEntity);
        Session updatedSession = sessionService.update(id, session);
        SessionDTO updatedDTO = sessionToDTOConverter.convert(updatedSession);
        log.info("Session updated");
        return ResponseEntity.ok(updatedDTO);
    }


    @Override
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        log.info("Deleting session with id: {}", id);
        sessionService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Get sessions for current user", description = "Retrieves all sessions for the current authenticated user")
    @GetMapping("/user-sessions")
    public ResponseEntity<List<SessionDTO>> getSessionIdsByUserId(@RequestParam String userId) {
        log.info("Retrieving sessions for user id: {}", userId);
        List<SessionDTO> sessions = sessionService.findSessionIdsByUserId(userId);
        return ResponseEntity.ok(sessions);
    }

}
