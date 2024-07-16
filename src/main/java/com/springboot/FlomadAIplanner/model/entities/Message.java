package com.springboot.FlomadAIplanner.model.entities;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.ai.chat.messages.Media;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.io.Serial;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Builder
@Document(collection = "messages")
public class Message extends Model implements org.springframework.ai.chat.messages.Message {
    @Serial
    private static final long serialVersionUID = -7214728696555033419L;

    @Field("sessionId")
    @Indexed
    @NotBlank
    private String sessionId;

    @Field("content")
    @NotBlank
    private String content;

    @Field("createdAt")
    @NotNull
    private LocalDateTime createdAt;

    @Field("messageType")
    @NotNull
    private MessageType messageType;

    @Field("metadata")
    private Map<String, Object> metadata;

    public Message(String id, String sessionId, String content, LocalDateTime createdAt, MessageType messageType, Map<String, Object> metadata) {
        super(id);
        this.sessionId = sessionId;
        this.content = content;
        this.createdAt = createdAt;
        this.messageType = messageType;
        this.metadata = metadata;
    }

    @Override
    public Map<String, Object> getMetadata() {
        return metadata;
    }

    @Override
    @SuppressWarnings("all")
    public List<Media> getMedia(String... dummy) {
        return null;
    }
}
