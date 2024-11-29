package com.springboot.FlomadAIplanner.service.socket;

import com.springboot.FlomadAIplanner.model.entities.Location;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class WebSocketService {
    private static final String LOCATION_DESTINATION = "/topic/locations/";
    private static final String MAP_CLEARED_MESSAGE = "mapCleared";
    private final SimpMessagingTemplate messagingTemplate;

    public WebSocketService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void sendLocationUpdate(Location location) {
        // Send the location update to clients subscribed to /topic/locations/{sessionId}
        String destination = LOCATION_DESTINATION + location.getSessionId();
        log.info("Sending location update to destination: {}", destination);
        messagingTemplate.convertAndSend(destination, location);
    }

    public void sendMapCleared(String sessionId) {
        String destination = LOCATION_DESTINATION + sessionId;
        log.info("Sending map cleared message to destination: {}", destination);
        messagingTemplate.convertAndSend(destination, MAP_CLEARED_MESSAGE);
    }
}
