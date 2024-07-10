package com.springboot.FlomadAIplanner.controller.model;

import com.springboot.FlomadAIplanner.dto.MessageDTO;
import com.springboot.FlomadAIplanner.model.entities.Message;
import com.springboot.FlomadAIplanner.service.model.MessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


/**
 * @class
 * @description This is a controller that allows you to talk to Gemini API
 */
// @CrossOrigin(origins = "http://localhost:4200")
@Slf4j
@RestController
@RequestMapping("/messagesPersistence")
public class MessageController {
    @Autowired
    private MessageService service;

    @PostMapping
    public ResponseEntity<Message> createMessage(@RequestBody MessageDTO dto) {
        log.info("Creating new message: {}", dto);
        Message newMessage = service.saveMessage(dto.getId(), dto.getMessage(), dto.getCreatedAt(), dto.getSpeaker());
        log.info("Message created");
        return ResponseEntity.ok(newMessage);
    }

    @GetMapping("/getSessionMessages")
    public ResponseEntity<List<Message>> getSessionMessages(@RequestParam String sessionId) {
        log.info("Retrieving messages for session: {}", sessionId);
        List<Message> messages = service.getMessagesBySessionId(sessionId);
        log.info("Messages retrieved");
        return ResponseEntity.ok(messages);
    }
}