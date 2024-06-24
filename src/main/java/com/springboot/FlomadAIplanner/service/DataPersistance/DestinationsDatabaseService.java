package com.springboot.FlomadAIplanner.service.DataPersistance;
import java.util.logging.Logger;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.springboot.FlomadAIplanner.DatabaseManagement.DestinationRepository;
import com.springboot.FlomadAIplanner.DatabaseManagement.Destinations;
import com.springboot.FlomadAIplanner.service.LLMService;

@Service
public class DestinationsDatabaseService {
    @Autowired
    private DestinationRepository destinationRepository;

    private static final Logger logger = Logger.getLogger(LLMService.class.getName());

    public Integer getDestinationIdByName(String destinationName) {
        logger.info("destinationName:"+destinationName);
        Destinations destination = destinationRepository.findByDestinationName(destinationName);
        logger.info("destination:"+destination);
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
            logger.info("First destination: " + destination.get());
            return destination.get();
        }
        logger.info("No destinations found.");
        return null;
    }
}
