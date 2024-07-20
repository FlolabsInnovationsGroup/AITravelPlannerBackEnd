package com.springboot.FlomadAIplanner.model.entities.login;

import com.springboot.FlomadAIplanner.model.entities.Model;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.io.Serial;

@Data
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
@Document(collection = "users")
public class User extends Model {
    @Serial
    private static final long serialVersionUID = -5739385634520071526L;

    @Field("username")
    @EqualsAndHashCode.Include
    @Indexed(unique = true)
    @NotBlank
    private String username;

    @Field("password")
    @NotBlank
    private String password;

    @Field("email")
    @Indexed(unique = true)
    @Email
    @NotBlank
    private String email;



    @Field("isEmailVerified")
    private Boolean isEmailVerified = false;
}
