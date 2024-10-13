package com.springboot.FlomadAIplanner.model.entities.login;

import com.springboot.FlomadAIplanner.model.entities.Model;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.io.Serial;

@Data
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
@Document(collection = "roles")
public class Role extends Model {
    @Serial
    private static final long serialVersionUID = 1L;

    @Field("name")
    @EqualsAndHashCode.Include
    @NotBlank(message = "Role name cannot be blank")
    private String name;  // Role name (e.g., ADMIN, USER)

    @Field("description")
    private String description;  // Optional description of the role
}

