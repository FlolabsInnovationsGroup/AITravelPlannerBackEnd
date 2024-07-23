package com.springboot.FlomadAIplanner.controller.model;

import com.springboot.FlomadAIplanner.converter.Converter;
import com.springboot.FlomadAIplanner.dto.MessageDTO;
import com.springboot.FlomadAIplanner.model.entities.Message;
import com.springboot.FlomadAIplanner.model.entities.login.User;
import com.springboot.FlomadAIplanner.service.model.MessageService;
import com.springboot.FlomadAIplanner.util.CurrentUserUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/messages")
public class MessageController implements CrudController<MessageDTO, String> {
    private final MessageService service;
    private final Converter<Message, MessageDTO> messageToDTOConverter;
    private final Converter<MessageDTO, Message> messageFromDTOConverter;
    private final CurrentUserUtil currentUserUtil;

    public MessageController(MessageService service, Converter<Message, MessageDTO> messageToDTOConverter,
                             Converter<MessageDTO, Message> messageFromDTOConverter, CurrentUserUtil currentUserUtil) {
        this.service = service;
        this.messageToDTOConverter = messageToDTOConverter;
        this.messageFromDTOConverter = messageFromDTOConverter;
        this.currentUserUtil = currentUserUtil;
    }

    @Override
    @PostMapping
    public ResponseEntity<MessageDTO> create(@RequestBody MessageDTO dto) {
        log.info("Creating new message: {}", dto);
        Message message = messageFromDTOConverter.convert(dto);
        Message newMessage = service.save(message);
        MessageDTO newMessageDTO = messageToDTOConverter.convert(newMessage);
        return ResponseEntity.ok(newMessageDTO);
    }

    @Override
    @GetMapping("/{id}")
    public ResponseEntity<MessageDTO> getById(@PathVariable String id) {
        log.info("Retrieving message by id: {}", id);
        Message message = service.findById(id);
        MessageDTO dto = messageToDTOConverter.convert(message);
        return ResponseEntity.ok(dto);
    }

    @Operation(summary = "Get messages for 'current user' only", description = "Retrieves all messages for the current authenticated user")
    @Override
    @GetMapping
    public ResponseEntity<List<MessageDTO>> getAll() {
        User user = currentUserUtil.getCurrentUserFromSecurityContext();
        log.info("Retrieving all messages for user: {}", user.getUsername());
        List<Message> messages = service.getMessagesByUserId(user.getId());
        List<MessageDTO> dtos = messages.stream()
                .map(messageToDTOConverter::convert)
                .toList();
        return ResponseEntity.ok(dtos);
    }

    @Override
    @PutMapping("/{id}")
    public ResponseEntity<MessageDTO> update(@PathVariable String id, @RequestBody MessageDTO updatedEntity) {
        log.info("Updating message with id: {}", id);
        Message updatedMessage = service.update(id, messageFromDTOConverter.convert(updatedEntity));
        MessageDTO updatedDTO = messageToDTOConverter.convert(updatedMessage);
        return ResponseEntity.ok(updatedDTO);
    }

    @Override
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        log.info("Deleting message with id: {}", id);
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Get messages by session ID", description = "Retrieves messages for a specific session by its ID")
    @GetMapping("/session/{sessionId}")
    public ResponseEntity<List<MessageDTO>> getMessagesBySessionId(@Parameter(description = "ID of the session to retrieve messages for", required = true) @PathVariable String sessionId) {
        log.info("Retrieving messages for session: {}", sessionId);
        List<Message> messages = service.getMessagesBySessionId(sessionId);
        List<MessageDTO> dtos = messages.stream()
                .map(messageToDTOConverter::convert)
                .toList();
        return ResponseEntity.ok(dtos);
    }

    @Operation(summary = "Get messages for current user", description = "Retrieves all messages for the current authenticated user")
    @GetMapping("/user-messages")
    public ResponseEntity<List<Message>> getMessagesByUserId() {
        User user = currentUserUtil.getCurrentUserFromSecurityContext();
        log.info("Retrieving messages for user: {}", user.getUsername());
        List<Message> messages = service.getMessagesByUserId(user.getUsername());
        return ResponseEntity.ok(messages);
    }
}
