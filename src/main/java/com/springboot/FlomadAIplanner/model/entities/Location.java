package com.springboot.FlomadAIplanner.model.entities;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document(collection = "locations")
public class Location extends Model {
    @Field("sessionId")
    @NotBlank
    private String sessionId; // Associates the location with a chat/session

    @Field("latitude")
    @NotNull
    private Double latitude;

    @Field("longitude")
    @NotNull
    private Double longitude;

    @Field("name")
    private String name; // Optional name or description of the location

    @Field("type")
    private String type; // E.g., "attraction", "cafe", "hotel"

    @Field("createdAt")
    private LocalDateTime createdAt;

    @Field("updatedAt")
    private LocalDateTime updatedAt;
}
