package com.springboot.FlomadAIplanner.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Set;

@Data
public class UserDTO {
    private String id;
    private String username;
    private String email;
    private Boolean isEmailVerified;
    private String provider;
    private Set<String> roles;  // Consider simplifying the roles from objects to a set of role names or IDs depending on the actual role definition
    private LocalDateTime lastActive;
}
