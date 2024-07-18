package com.springboot.FlomadAIplanner.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.ai.chat.messages.MessageType;

import java.time.LocalDateTime;
import java.util.Map;


/**
 * @class
 * @description This is a class store data sent from the front end, in this case, store a new message.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageDTO {
    private String id;
    private String sessionId;
    private String content;
    private LocalDateTime createdAt;
    private MessageType messageType;
    private Map<String, String> metadata;
    
}
