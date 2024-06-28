package com.springboot.FlomadAIplanner.DTO;

import lombok.Data;

import java.time.LocalDateTime;


/**
 * @class
 * @description This is a class store data retrived from database(all messages in a session) and ready to send to front end
 */
@Data
public class SessionDto {
    private String sessionId;
    private String userId;
    private String title;
    private LocalDateTime timestamp;

    public SessionDto(String sessionId, String userId, String title, LocalDateTime timestamp) {
        this.sessionId = sessionId;
        this.userId = userId;
        this.title = title;
        this.timestamp = timestamp;
    }

}
