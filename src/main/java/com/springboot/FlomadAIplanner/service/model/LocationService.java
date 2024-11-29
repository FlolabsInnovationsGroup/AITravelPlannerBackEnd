package com.springboot.FlomadAIplanner.service.model;

import com.springboot.FlomadAIplanner.model.entities.Location;
import com.springboot.FlomadAIplanner.model.repository.LocationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class LocationService {
    private final LocationRepository locationRepository;

    public LocationService(LocationRepository locationRepository) {
        this.locationRepository = locationRepository;
    }

    public Location save(Location location) {
        log.info("Saving location: {}", location);
        location.setCreatedAt(LocalDateTime.now());
        location.setUpdatedAt(LocalDateTime.now());
        return locationRepository.save(location);
    }

    public Location findById(String id) {
        log.debug("Finding location by ID: {}", id);
        return locationRepository.findById(id).orElse(null);
    }

    public List<Location> findAllBySessionId(String sessionId) {
        log.debug("Finding all locations by session ID: {}", sessionId);
        return locationRepository.findBySessionId(sessionId);
    }


    public Location update(String id, Location updatedEntity) {
        log.info("Updating location with ID: {}", id);
        updatedEntity.setUpdatedAt(LocalDateTime.now());
        return locationRepository.save(updatedEntity);
    }

    public void delete(String id) {
        log.info("Deleting location with ID: {}", id);
        locationRepository.deleteById(id);
    }

    public void deleteAllBySessionId(String sessionId) {
        log.info("Deleting all locations for session ID: {}", sessionId);
        List<Location> locations = locationRepository.findBySessionId(sessionId);
        locationRepository.deleteAll(locations);
    }
}
