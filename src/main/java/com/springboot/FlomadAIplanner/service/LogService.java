package com.springboot.FlomadAIplanner.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class LogService {
    private static final Logger logger = LoggerFactory.getLogger(LogService.class);

    public void performAction() {
        try {
            // Your business logic here
            throw new RuntimeException("An error occurred");
        } catch (Exception e) {
            String trackId = UUID.randomUUID().toString();
            logger.error("Exception occurred with trackId: {}", trackId, e);
        }
    }
}

