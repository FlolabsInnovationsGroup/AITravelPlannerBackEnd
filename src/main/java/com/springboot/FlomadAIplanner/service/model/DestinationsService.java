package com.springboot.FlomadAIplanner.service.model;

import com.springboot.FlomadAIplanner.model.entities.Destination;
import com.springboot.FlomadAIplanner.model.repository.DestinationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
public class DestinationsService {
    @Autowired
    private DestinationRepository destinationRepository;


    public Integer getDestinationIdByName(String destinationName) {
        log.info("destinationName:" + destinationName);
        Destination destination = destinationRepository.findByDestinationName(destinationName);
        log.info("destination:" + destination);
        if (destination != null) {
            return destination.getDestinationId();
        }
        return null;
    }

    public long countDestinations() {
        return destinationRepository.count();
    }

    public Destination getFirstDestination() {
        Optional<Destination> destination = destinationRepository.findAll().stream().findFirst();
        if (destination.isPresent()) {
            log.info("First destination: " + destination.get());
            return destination.get();
        }
        log.info("No destinations found.");
        return null;
    }
}
