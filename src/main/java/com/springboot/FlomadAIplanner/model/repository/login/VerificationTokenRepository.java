package com.springboot.FlomadAIplanner.model.repository.login;

import com.springboot.FlomadAIplanner.model.entities.login.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface VerificationTokenRepository extends JpaRepository<VerificationToken, String> {

    @Query("SELECT v FROM VerificationToken v WHERE v.user.id = :userId")
    VerificationToken findByUserId(@Param("userId") String userId);

    int deleteByUserId(String userId); // Custom method to delete token by user ID

}
