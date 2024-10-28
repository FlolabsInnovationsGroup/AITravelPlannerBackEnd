package com.springboot.FlomadAIplanner.model.repository;

import com.springboot.FlomadAIplanner.model.entities.Message;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

/**
 * @class
 * @description This is a MongoDB repository object, where you can define database operation functions like findBySessionId, MongoDB repository object will read the function
 * name and infer the intent of the coder. This is an interface with the database.
 */
@Repository
public interface MessageRepository extends MongoRepository<Message, String> {
    List<Message> findAllBySessionIdOrderByCreatedAtAsc(String sessionId);

    List<Message> findBySessionIdInOrderByCreatedAtAsc(List<String> sessionIds);


    List<Message> findBySessionIdOrderByCreatedAtDesc(String sessionId, Pageable pageable);

    Long countBySessionIdInAndMessageType(Collection<@NotBlank String> sessionIds, @NotNull MessageType messageType);

    void deleteBySessionId(String sessionId);
}
