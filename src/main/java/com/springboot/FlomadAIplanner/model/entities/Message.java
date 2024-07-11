package com.springboot.FlomadAIplanner.model.entities;

import com.springboot.FlomadAIplanner.model.util.MetadataConverter;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.ai.chat.messages.Media;
import org.springframework.ai.chat.messages.MessageType;

import java.io.Serial;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "message")
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Builder
public class Message extends Model implements org.springframework.ai.chat.messages.Message {
    @Serial
    private static final long serialVersionUID = -7214728696555033419L;

    @Column(name = "session_id", nullable = false)
    private String sessionId;

    @Column(name = "content", length = 10000)
    private String content;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "message_type", nullable = false)
    private MessageType messageType;

    @Convert(converter = MetadataConverter.class)
    @Column(name = "metadata", columnDefinition = "json", length = 5000)
    private Map<String, Object> metadata;

    public Message(String id, String sessionId, String content, LocalDateTime createdAt, MessageType messageType, Map<String, Object> metadata) {
        this.sessionId = sessionId;
        this.content = content;
        this.createdAt = createdAt;
        this.messageType = messageType;
        this.metadata = metadata;
    }

    @Override
    public Map<String, Object> getMetadata() {
        return null;
    }

    @Override
    @Deprecated
    @SuppressWarnings("all")
    public List<Media> getMedia(String... dummy) {
        return null;
    }

}
