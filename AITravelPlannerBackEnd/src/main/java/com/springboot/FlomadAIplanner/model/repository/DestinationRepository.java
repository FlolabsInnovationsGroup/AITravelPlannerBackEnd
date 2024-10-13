package com.springboot.FlomadAIplanner.model.repository;

import com.springboot.FlomadAIplanner.model.entities.Destination;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DestinationRepository extends MongoRepository<Destination, String> {
    Destination findByDestinationName(String destinationName);

    // @Query("{'destinationName': {$regex: ?0, $options: 'i'}}")
    // Destination findByDestinationNameIgnoreCase(String destinationName);
}
