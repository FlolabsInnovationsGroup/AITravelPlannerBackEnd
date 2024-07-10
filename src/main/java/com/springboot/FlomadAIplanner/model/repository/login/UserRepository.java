package com.springboot.FlomadAIplanner.model.repository.login;

import com.springboot.FlomadAIplanner.model.entities.login.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByUsername(String username);
    User findByVerificationToken(String verificationToken);
}
