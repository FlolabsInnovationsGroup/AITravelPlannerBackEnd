package com.springboot.FlomadAIplanner.controller.model;

import com.springboot.FlomadAIplanner.converter.Converter;
import com.springboot.FlomadAIplanner.dto.MessageDTO;
import com.springboot.FlomadAIplanner.model.entities.Message;
import com.springboot.FlomadAIplanner.service.model.MessageService;
import com.springboot.FlomadAIplanner.util.JwtUtil;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
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
    private final MessageService service;
    private final Converter<Message, MessageDTO> messageToDTOConverter;
    private final Converter<MessageDTO, Message> messageFromDTOConverter;
    private JwtUtil jwtUtil;

    public MessageController(MessageService service, Converter<Message, MessageDTO> messageToDTOConverter, Converter<MessageDTO, Message> messageFromDTOConverter,JwtUtil jwtUtil) {
        this.service = service;
        this.messageToDTOConverter = messageToDTOConverter;
        this.messageFromDTOConverter = messageFromDTOConverter;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping
    public ResponseEntity<Message> createMessage(@RequestBody MessageDTO dto, HttpServletRequest request) {
        log.info("Creating new message: {}", dto);
        final String authorizationHeader = request.getHeader("Authorization");
        String username = null;
        String jwt = null;
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwt = authorizationHeader.substring(7);
            username = jwtUtil.extractUsername(jwt);
        }
        dto.setUsername(username);
        log.info("message dto : {}", dto);
        Message message = messageFromDTOConverter.convert(dto);
        Message newMessage = service.save(message);

        log.info("Message created");
        return ResponseEntity.ok(newMessage);
    }

    @GetMapping("/getSessionMessages")
    public ResponseEntity<List<MessageDTO>> getSessionMessages(@RequestParam String sessionId) {
        log.info("Retrieving messages for session: {}", sessionId);

        List<Message> messages = service.getMessagesBySessionId(sessionId);
        log.info("messages of the session: {}", messages);

        List<MessageDTO> dtos = messages.stream()
                .map(messageToDTOConverter::convert)
                .toList();
        log.info("Messages retrieved");
        return ResponseEntity.ok(dtos);
    }
}