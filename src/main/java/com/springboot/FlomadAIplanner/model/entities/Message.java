package com.springboot.FlomadAIplanner.model.entities;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;


/**
 * @class
 * @description This is an entity, a reflection of the database, provide a straight forward way to operate the database
 */
@Data
@Entity
@Table(name = "message")
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "message_id")
    private int messageId;

    @Column(name = "session_id", nullable = false)
    private String sessionId;

    @Column(name = "content")
    private String content;

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    @Enumerated(EnumType.STRING)
    @Column(name = "speaker", nullable = false)
    private Speaker speaker;

    public Message() {
    }

}
