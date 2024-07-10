package com.springboot.FlomadAIplanner.dto;

import com.springboot.FlomadAIplanner.model.entities.Speaker;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


/**
 * @class
 * @description This is a class store data sent from the front end, in this case, store a new message.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageDTO {
    private String id;
    private String message;
    private LocalDateTime createdAt;
    private Speaker speaker;
}
