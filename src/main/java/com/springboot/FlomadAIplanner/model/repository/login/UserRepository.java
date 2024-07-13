package com.springboot.FlomadAIplanner.model.repository.login;

import org.springframework.data.jpa.repository.JpaRepository;

import com.springboot.FlomadAIplanner.model.entities.login.User;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByVerificationToken(String token);
    User findByUsername(String username);
}
