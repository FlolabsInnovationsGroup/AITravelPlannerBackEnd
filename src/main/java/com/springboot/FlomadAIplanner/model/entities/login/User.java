package com.springboot.FlomadAIplanner.model.entities.login;

import com.springboot.FlomadAIplanner.model.entities.Model;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

@Entity
@Table(name = "users")
@Data
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
public class User extends Model {
    @Serial
    private static final long serialVersionUID = -5739385634520071526L;

    @Column(name = "username", nullable = false, unique = true)
    @EqualsAndHashCode.Include
    private String username;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "email", unique = true)
    private String email;

    @Column(name = "is_verified", nullable = false)
    private boolean isVerified = false;

    @Column(name = "enabled", nullable = false)
    private boolean enabled = false;

    @Column(name = "is_email_verified", nullable = false, columnDefinition = "boolean default false")
    private Boolean isEmailVerified = false;

}
