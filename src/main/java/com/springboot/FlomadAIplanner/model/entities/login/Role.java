package com.springboot.FlomadAIplanner.model.entities.login;

import com.springboot.FlomadAIplanner.model.entities.Model;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.io.Serial;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
@Document(collection = "roles")
public class Role extends Model {
    @Serial
    private static final long serialVersionUID = -2156729485039174628L;

    @Field("name")
    @EqualsAndHashCode.Include
    @NotBlank(message = "Role name cannot be blank")
    private String name;  // Role name (e.g., ADMIN, USER)

    @Field("description")
    private String description;  // Optional description of the role

    public Role(String id, String name, String description) {
        super(id);
        this.name = name;
        this.description = description;
    }

}

