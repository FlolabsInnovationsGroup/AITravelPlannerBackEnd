package com.springboot.FlomadAIplanner.model.repository;

import com.springboot.FlomadAIplanner.model.entities.Location;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LocationRepository extends MongoRepository<Location, String> {
    /**
     * Finds all locations associated with a given session ID.
     *
     * @param sessionId The session ID to search for.
     * @return A list of matching locations.
     */
    List<Location> findBySessionId(String sessionId);

    /**
     * Deletes all locations associated with a given session ID.
     *
     * @param sessionId The session ID for which locations should be deleted.
     */
    @Query("{ 'sessionId': ?0 }")
    void deleteBySessionId(String sessionId);
}
