package com.springboot.FlomadAIplanner.model.repository.login;

import com.springboot.FlomadAIplanner.model.entities.login.VerificationToken;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface VerificationTokenRepository extends MongoRepository<VerificationToken, String> {
    @Query("{ 'user.id': ?0 }")
    VerificationToken findByUserId(String userId);

    int deleteByUserId(String userId); // Custom method to delete token by user ID
}
