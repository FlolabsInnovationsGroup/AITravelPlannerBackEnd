package com.springboot.FlomadAIplanner.model.entities;

import jakarta.persistence.PrePersist;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Document // Adding @Document to indicate this is a MongoDB document
public abstract class Model implements Serializable {
    @Serial
    private static final long serialVersionUID = 7945147474269998569L;

    @Id
    private String id;

    @PrePersist
    public void generateId() {
        if (this.id == null) {
            this.id = UUID.randomUUID().toString();
        }
    }
}
