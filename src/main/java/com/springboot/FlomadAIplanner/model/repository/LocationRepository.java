package com.springboot.FlomadAIplanner.model.repository;

import com.springboot.FlomadAIplanner.model.entities.Location;
import org.springframework.data.mongodb.repository.MongoRepository;
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
    void deleteBySessionId(String sessionId);
}
