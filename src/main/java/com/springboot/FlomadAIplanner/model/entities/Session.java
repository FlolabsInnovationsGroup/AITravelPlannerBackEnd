package com.springboot.FlomadAIplanner.model.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "session")
public class Session {
    @Id
    private String sessionId;
    private String userId;
    private String title;
    private LocalDateTime timestamp;

}
