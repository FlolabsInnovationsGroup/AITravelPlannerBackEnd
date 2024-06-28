package com.springboot.FlomadAIplanner.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


/**
 * @class
 * @description This is a class store data retrived from database(all messages in a session) and ready to send to front end
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SessionDto {
    private String sessionId;
    private String userId;
    private String title;
    private LocalDateTime timestamp;

}
