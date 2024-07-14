package com.springboot.FlomadAIplanner.model.repository.login;

import com.springboot.FlomadAIplanner.model.entities.login.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    User findByUsername(String username);

    @Query("SELECT v.user FROM VerificationToken v WHERE v.id = :verificationTokenId")
    User findUserByVerificationToken(@Param("verificationTokenId") String verificationTokenId);

}
