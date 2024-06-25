package com.springboot.FlomadAIplanner.controller.DataPersistance;

import com.springboot.FlomadAIplanner.DatabaseManagement.MessagesDto;
import com.springboot.FlomadAIplanner.DatabaseManagement.NewMessage;
import com.springboot.FlomadAIplanner.service.ConversationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


////////////////////////////////////////////

/**
 * @class
 * @description This is a controller that allows you to talk to Gemini API
 */
//////////////////////////////////////////
// @CrossOrigin(origins = "http://localhost:4200")
@Slf4j
@RestController
@RequestMapping("/messagesPersistence")
public class MessageController {
    @Autowired
    private ConversationService service;

    @PostMapping
    public ResponseEntity<NewMessage> createMessage(@RequestBody MessagesDto dto) {
        log.info("Creating new message: {}", dto);
        NewMessage newMessage = service.saveMessage(dto.getID(), dto.getMessage(), dto.getTimestamp(), dto.getSpeaker());
        log.info("Message created");
        return ResponseEntity.ok(newMessage);
    }

    @GetMapping("/getSessionMessages")
    public ResponseEntity<List<NewMessage>> getSessionMessages(@RequestParam String sessionId) {
        log.info("Retrieving messages for session: {}", sessionId);
        List<NewMessage> messages = service.getMessagesBySessionId(sessionId);
        log.info("Messages retrieved");
        return ResponseEntity.ok(messages);
    }
}