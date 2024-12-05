package com.springboot.FlomadAIplanner.service.socket;

import com.springboot.FlomadAIplanner.model.entities.Location;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class WebSocketService {
    private static final String LOCATION_DESTINATION = "/topic/locations/";
    private final SimpMessagingTemplate messagingTemplate;

    public WebSocketService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void sendLocationUpdate(Location location) {
        String destination = LOCATION_DESTINATION + location.getSessionId();
        log.info("Sending LOCATION_ADDED to destination: {}", destination);

        Map<String, Object> message = new HashMap<>();
        message.put("type", "LOCATION_ADDED");
        message.put("payload", location);

        messagingTemplate.convertAndSend(destination, message);
    }

    public void sendMapCleared(String sessionId) {
        String destination = LOCATION_DESTINATION + sessionId;
        log.info("Sending MAP_CLEARED to destination: {}", destination);

        Map<String, Object> message = new HashMap<>();
        message.put("type", "MAP_CLEARED");
        message.put("payload", null);

        messagingTemplate.convertAndSend(destination, message);
    }

    public void sendLocationRemoved(String sessionId, String locationId) {
        String destination = LOCATION_DESTINATION + sessionId;
        log.info("Sending LOCATION_REMOVED to destination: {}", destination);

        Map<String, Object> message = new HashMap<>();
        message.put("type", "LOCATION_REMOVED");
        Map<String, String> payload = new HashMap<>();
        payload.put("locationId", locationId);
        message.put("payload", payload);

        messagingTemplate.convertAndSend(destination, message);
    }

}
