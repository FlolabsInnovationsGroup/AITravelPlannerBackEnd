package com.springboot.FlomadAIplanner.model.repository;

import com.springboot.FlomadAIplanner.model.entities.Destination;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface DestinationRepository extends JpaRepository<Destination, Integer> {
    Destination findByDestinationName(String destinationName);

    // @Query("SELECT d FROM Destination d WHERE LOWER(d.destinationName) = LOWER(:destinationName)")
    // Destination findByDestinationNameIgnoreCase(@Param("destinationName") String destinationName);

}