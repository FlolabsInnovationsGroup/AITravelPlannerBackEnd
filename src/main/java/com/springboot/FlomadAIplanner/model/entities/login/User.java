package com.springboot.FlomadAIplanner.model.entities.login;

import com.springboot.FlomadAIplanner.model.entities.Model;
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
    private String username;

    @Field("password")
    private String password;

    @Field("email")
    @Indexed(unique = true)
    private String email;

    @Field("isVerified")
    private boolean isVerified = false;

    @Field("enabled")
    private boolean enabled = false;

    @Field("isEmailVerified")
    private Boolean isEmailVerified = false;
}
