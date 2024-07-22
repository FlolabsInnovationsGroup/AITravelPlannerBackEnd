package com.springboot.FlomadAIplanner.model.repository;

import com.springboot.FlomadAIplanner.model.entities.Message;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @class
 * @description This is a MongoDB repository object, where you can define database operation functions like findBySessionId, MongoDB repository object will read the function
 * name and infer the intent of the coder. This is an interface with the database.
 */
@Repository
public interface MessageRepository extends MongoRepository<Message, String> {
    List<Message> findAllBySessionIdOrderByCreatedAtAsc(String sessionId);

    List<Message> findAllByUsernameOrderByCreatedAtAsc(String username);

    List<Message> findBySessionIdOrderByCreatedAtDesc(String sessionId, Pageable pageable);

    void deleteBySessionId(String sessionId);
}
