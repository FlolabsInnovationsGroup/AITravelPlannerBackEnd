package com.springboot.FlomadAIplanner.model.entities;

import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.io.Serial;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
@Document(collection = "sessions")
public class SessionTokenVersion extends Model {
    @Serial
    private static final long serialVersionUID = 7672693738433375044L;

    @Id 
    @Field("sessionId")
    @NotBlank
    @Indexed(unique = true)
    private String sessionId;

    @Field("userId")
    @Indexed
    @NotBlank
    private String userId;

    @Field("title")
    @NotBlank
    private String title;

    @Field("createdAt")
    @NotNull
    private LocalDateTime createdAt;

    @Field("updatedAt")
    @NotNull
    private LocalDateTime updatedAt;

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId=sessionId;
    }
    
}