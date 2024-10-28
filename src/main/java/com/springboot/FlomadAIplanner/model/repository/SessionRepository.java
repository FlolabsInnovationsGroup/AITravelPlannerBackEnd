package com.springboot.FlomadAIplanner.model.repository;

import com.springboot.FlomadAIplanner.model.entities.Session;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @class
 * @description This is a MongoDB repository object, where you can define database operation functions like findByUserId, MongoDB repository object will read the function
 * name and infer the intent of the coder. This is an interface with the database.
 */
@Repository
public interface SessionRepository extends MongoRepository<Session, String> {
    List<Session> findByUserId(String userId);

    List<Session> findSessionByUserId(@NotBlank String userId);

    Session findByIdAndUserId(String sessionId, String userId); // For updating the session's title and timestamp
}
