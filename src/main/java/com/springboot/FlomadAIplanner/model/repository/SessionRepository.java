package com.springboot.FlomadAIplanner.model.repository;

import com.springboot.FlomadAIplanner.model.entities.Session;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


/**
 * @class
 * @description This is a Jpa object, where you can define datebase operation functions like findByUserId, Jpa object will read the function
 * name and infer the intent of coder. This is an interface with the database.
 */
@Repository
public interface SessionRepository extends JpaRepository<Session, String> {
    List<Session> findByUserId(String userId);

    Session findBySessionIdAndUserId(String sessionId, String userId); //For updating the session's tittle and timestamp
}