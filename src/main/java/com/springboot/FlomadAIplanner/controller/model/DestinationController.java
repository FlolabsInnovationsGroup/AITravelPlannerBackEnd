package com.springboot.FlomadAIplanner.controller.model;

import com.springboot.FlomadAIplanner.model.entities.Destination;
import com.springboot.FlomadAIplanner.service.model.DestinationsService;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/destinations")
public class DestinationController implements CrudController<Destination, String> {
    private final DestinationsService destinationService;

    public DestinationController(DestinationsService destinationService) {
        this.destinationService = destinationService;
    }

    @Override
    @Hidden
    @PostMapping
    public ResponseEntity<Destination> create(@RequestBody Destination entity) {
        log.info("Creating new destination: {}", entity);
        Destination newDestination = destinationService.save(entity);
        return ResponseEntity.ok(newDestination);
    }

    @Override
    @Hidden
    @GetMapping("/{id}")
    public ResponseEntity<Destination> getById(@PathVariable String id) {
        log.info("Retrieving destination by id: {}", id);
        Destination destination = destinationService.findById(id);
        return ResponseEntity.ok(destination);
    }

    @Override
    @Hidden
    @GetMapping
    public ResponseEntity<List<Destination>> getAll() {
        log.info("Retrieving all destinations");
        List<Destination> destinations = destinationService.findAll();
        return ResponseEntity.ok(destinations);
    }

    @Override
    @Hidden
    @PutMapping("/{id}")
    public ResponseEntity<Destination> update(@PathVariable String id, @RequestBody Destination updatedEntity) {
        log.info("Updating destination with id: {}", id);
        Destination updatedDestination = destinationService.update(id, updatedEntity);
        return ResponseEntity.ok(updatedDestination);
    }

    @Override
    @Hidden
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        log.info("Deleting destination with id: {}", id);
        destinationService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Get destination ID by name", description = "Retrieves the destination ID for a given destination name")
    @Hidden
    @GetMapping("/getDestinationId")
    public Integer getDestinationId(@Parameter(description = "Name of the destination", required = true) @RequestParam String destinationName) {
        log.info("Retrieving destination id for destination: {}", destinationName);
        return destinationService.getDestinationIdByName(destinationName);
    }

    @Operation(summary = "Count all destinations", description = "Counts the total number of destinations")
    @Hidden
    @GetMapping("/countDestinations")
    public long countDestinations() {
        log.info("Counting destinations");
        return destinationService.countDestinations();
    }

    @Operation(summary = "Get first destination", description = "Retrieves the first destination")
    @Hidden
    @GetMapping("/getFirstDestination")
    public Destination getFirstDestination() {
        log.info("Retrieving first destination");
        return destinationService.getFirstDestination();
    }
}
