package com.springboot.FlomadAIplanner.model.entities.login;

import com.springboot.FlomadAIplanner.model.entities.Model;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.io.Serial;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Data
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
@Document(collection = "users")
public class User extends Model {
    @Serial
    private static final long serialVersionUID = -5739385634520071526L;

    @Field("username")
    @EqualsAndHashCode.Include
    @Indexed(unique = true) // (unique = true, sparse = true) when username is not required
    @NotBlank(message = "Username cannot be blank")
    private String username;

    @Field("password")
    private String password;

    @Field("email")
    @Indexed(unique = true, sparse = true)
    @Email(message = "Email should be valid")
    private String email;

    @Field("isEmailVerified")
    private Boolean isEmailVerified = false;

    @Field("provider")
    private String provider;

    @DBRef
    @Field("roles")
    private Set<Role> roles = new HashSet<>();  //private Set<Role> roles = new, HashSet<>(); this ensures that even if no roles are assigned, roles will not be null.

    @Field("lastActive")
    private LocalDateTime lastActive;
}
