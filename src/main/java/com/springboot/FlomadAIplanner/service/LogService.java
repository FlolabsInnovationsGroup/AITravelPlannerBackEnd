package com.springboot.FlomadAIplanner.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.UUID;

/** Service for logging and error tracking. */
@Service
public final class LogService {

    /** Logger instance. */
    private static final Logger LOGGER =
            LoggerFactory.getLogger(LogService.class);

    /**
     * Performs the configured action; logs exceptions with a track ID.
     */
    public void performAction() {
        try {
            throw new RuntimeException("An error occurred");
        } catch (Exception e) {
            String trackId = UUID.randomUUID().toString();
            LOGGER.error("Exception occurred with trackId: {}", trackId, e);
        }
    }
}
