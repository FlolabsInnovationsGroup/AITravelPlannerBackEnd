package com.springboot.FlomadAIplanner.model.entities;

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
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
@Document(collection = "sessions")
public class Session extends Model {
    @Serial
    private static final long serialVersionUID = 7672693738433375044L;

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

    @Field("messages")
    private List<String> messages = new ArrayList<>();

    public Session(String id, String userId, String title, LocalDateTime createdAt, LocalDateTime updatedAt) {
        super(id);
        this.userId = userId;
        this.title = title;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

}
