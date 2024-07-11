package com.springboot.FlomadAIplanner.model.entities.login;

import lombok.Data;
import lombok.EqualsAndHashCode;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.util.UUID;
@Entity
@Table(name = "users")
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    @EqualsAndHashCode.Include
    private String username;

    private String password;

    @Column(unique = true)
    private String email;

    @Column(nullable = false)
    private boolean isVerified = false;

    @Column(nullable = false)
    private String verificationToken;

    @Column(nullable = false)
    private boolean enabled = false;

    @PrePersist
    private void prePersist() {
        this.verificationToken = UUID.randomUUID().toString();
        this.isVerified = false;
        this.enabled = false;
    }

    @Column(nullable = false, columnDefinition = "boolean default false")
    private Boolean isEmailVerified = false;

    // Getter and setter for isVerified
    public boolean isVerified() {
        return isVerified;
    }

    public void setIsVerified(boolean isVerified) {
        this.isVerified = isVerified;
    }

    // Getter and setter for isEmailVerified
    public Boolean getIsEmailVerified() {
        return isEmailVerified;
    }

    public void setIsEmailVerified(Boolean isEmailVerified) {
        this.isEmailVerified = isEmailVerified;
    }

    // Getter and setter for verificationToken
    public String getVerificationToken() {
        return verificationToken;
    }

    public void setVerificationToken(String verificationToken) {
        this.verificationToken = verificationToken;
    }

    // Getter and setter for enabled
    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
