package com.springboot.FlomadAIplanner.DTO;

import com.springboot.FlomadAIplanner.model.entities.Speaker;
import lombok.Data;

import java.time.LocalDateTime;


/**
 * @class
 * @description This is a class store data sent from the front end, in this case, store a new message.
 */
@Data
public class MessageDTO {
    private Speaker speaker;
    private String message;
    private LocalDateTime timestamp;
    private String id;

}
