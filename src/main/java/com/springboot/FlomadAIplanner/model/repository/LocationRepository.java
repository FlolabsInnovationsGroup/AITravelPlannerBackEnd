package com.springboot.FlomadAIplanner.model.repository;

import com.springboot.FlomadAIplanner.model.entities.Location;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LocationRepository extends MongoRepository<Location, String> {
    List<Location> findBySessionId(String sessionId);

    List<Location> findAllBySessionId(String sessionId);

}
