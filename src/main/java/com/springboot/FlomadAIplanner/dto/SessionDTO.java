package com.springboot.FlomadAIplanner.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @class
 * @description This is a class that stores data retrieved from the database (all messages in a session) and is ready to send to the front end.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class SessionDTO {
    @EqualsAndHashCode.Include
    private String sessionId;
    private String userId;
    private String title;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String userType; // Lombok will automatically generate the getter and setter
}


