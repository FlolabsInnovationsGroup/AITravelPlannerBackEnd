package com.springboot.FlomadAIplanner.model.repository;

import com.springboot.FlomadAIplanner.model.entities.Session;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * @class
 * @description This is a MongoDB repository object, where you can define database operation functions like findByUserId, MongoDB repository object will read the function
 * name and infer the intent of the coder. This is an interface with the database.
 */
@Repository
public interface SessionRepository extends MongoRepository<Session, String> {
    List<Session> findByUserId(String userId);

    Session findByIdAndUserId(String sessionId, String userId); // For updating the session's title and timestamp

    Optional<Session> findByUserIdIsNull();
}
