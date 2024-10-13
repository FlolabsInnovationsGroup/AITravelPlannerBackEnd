package com.springboot.FlomadAIplanner.service.model;

import com.springboot.FlomadAIplanner.model.entities.Destination;
import com.springboot.FlomadAIplanner.model.repository.DestinationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DestinationsService implements CrudService<Destination, String> {

    private final DestinationRepository destinationRepository;

    @Override
    public Destination save(Destination destination) {
        log.info("Saving destination: {}", destination);
        return destinationRepository.save(destination);
    }

    @Override
    public Destination findById(String id) {
        log.info("Finding destination by id: {}", id);
        return destinationRepository.findById(id).orElse(null);
    }

    @Override
    public List<Destination> findAll() {
        log.info("Finding all destinations");
        return destinationRepository.findAll();
    }

    @Override
    public Destination update(String id, Destination updatedDestination) {
        log.info("Updating destination with id: {}", id);
        updatedDestination.setId(id);
        return destinationRepository.save(updatedDestination);
    }

    @Override
    public void delete(String id) {
        log.info("Deleting destination with id: {}", id);
        destinationRepository.deleteById(id);
    }

    public Integer getDestinationIdByName(String destinationName) {
        log.info("Finding destination id by name: {}", destinationName);
        Destination destination = destinationRepository.findByDestinationName(destinationName);
        return destination != null ? destination.getDestinationId() : null;
    }

    public long countDestinations() {
        log.info("Counting all destinations");
        return destinationRepository.count();
    }

    public Destination getFirstDestination() {
        log.info("Finding the first destination");
        Optional<Destination> destination = destinationRepository.findAll().stream().findFirst();
        return destination.orElse(null);
    }
}
