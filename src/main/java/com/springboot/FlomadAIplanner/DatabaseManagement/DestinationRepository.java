package com.springboot.FlomadAIplanner.DatabaseManagement;

import org.springframework.data.jpa.repository.JpaRepository;


public interface DestinationRepository extends JpaRepository<Destinations, Integer> {
    Destinations findByDestinationName(String destinationName);

    // @Query("SELECT d FROM Destination d WHERE LOWER(d.destinationName) = LOWER(:destinationName)")
    // Destination findByDestinationNameIgnoreCase(@Param("destinationName") String destinationName);

}