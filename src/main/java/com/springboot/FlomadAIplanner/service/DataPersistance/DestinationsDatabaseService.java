package com.springboot.FlomadAIplanner.service.DataPersistance;

import com.springboot.FlomadAIplanner.DatabaseManagement.DestinationRepository;
import com.springboot.FlomadAIplanner.DatabaseManagement.Destinations;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
public class DestinationsDatabaseService {
    @Autowired
    private DestinationRepository destinationRepository;


    public Integer getDestinationIdByName(String destinationName) {
        log.info("destinationName:" + destinationName);
        Destinations destination = destinationRepository.findByDestinationName(destinationName);
        log.info("destination:" + destination);
        if (destination != null) {
            return destination.getDestinationId();
        }
        return null;
    }

    public long countDestinations() {
        return destinationRepository.count();
    }

    public Destinations getFirstDestination() {
        Optional<Destinations> destination = destinationRepository.findAll().stream().findFirst();
        if (destination.isPresent()) {
            log.info("First destination: " + destination.get());
            return destination.get();
        }
        log.info("No destinations found.");
        return null;
    }
}
