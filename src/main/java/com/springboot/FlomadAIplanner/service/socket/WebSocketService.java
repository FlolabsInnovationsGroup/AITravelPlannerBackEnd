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
        if (location == null || location.getSessionId() == null) {
            log.warn("Invalid location or sessionId for LOCATION_ADDED event.");
            return;
        }
        String destination = LOCATION_DESTINATION + location.getSessionId();
        log.info("Sending LOCATION_ADDED to destination: {}", destination);

        Map<String, Object> message = createMessage("LOCATION_ADDED", location);

        try {
            messagingTemplate.convertAndSend(destination, message);
            log.debug("LOCATION_ADDED message sent successfully.");
        } catch (Exception e) {
            log.error("Failed to send LOCATION_ADDED message for sessionId: {} to {}: {}", location.getSessionId(), destination, e.getMessage());
        }
    }

    public void sendMapCleared(String sessionId) {
        if (!isValidSessionId(sessionId)) {
            log.warn("Invalid sessionId provided for MAP_CLEARED event.");
            return;
        }
        String destination = LOCATION_DESTINATION + sessionId;

        log.info("Sending MAP_CLEARED to destination: {}", destination);

        Map<String, Object> message = createMessage("MAP_CLEARED", new HashMap<>());

        try {
            messagingTemplate.convertAndSend(destination, message);
            log.debug("MAP_CLEARED message sent successfully.");
        } catch (Exception e) {
            log.error("Failed to send MAP_CLEARED message for sessionId: {} to {}: {}", sessionId, destination, e.getMessage());
        }
    }

    public void sendLocationRemoved(String sessionId, String locationId) {
        if (!isValidSessionId(sessionId) || locationId == null || locationId.trim().isEmpty()) {
            log.warn("Invalid sessionId or locationId provided for LOCATION_REMOVED event.");
            return;
        }
        String destination = LOCATION_DESTINATION + sessionId;
        log.info("Sending LOCATION_REMOVED to destination: {}", destination);

        Map<String, String> payload = new HashMap<>();
        payload.put("locationId", locationId);

        Map<String, Object> message = createMessage("LOCATION_REMOVED", payload);

        try {
            messagingTemplate.convertAndSend(destination, message);
            log.debug("LOCATION_REMOVED message sent successfully.");
        } catch (Exception e) {
            log.error("Failed to send LOCATION_REMOVED message for sessionId: {}, locationId: {} to {}: {}", sessionId, locationId, destination, e.getMessage());
        }
    }

    private Map<String, Object> createMessage(String type, Object payload) {
        Map<String, Object> message = new HashMap<>();
        message.put("type", type);
        message.put("payload", payload);
        return message;
    }

    private boolean isValidSessionId(String sessionId) {
        return sessionId != null && !sessionId.trim().isEmpty();
    }
}

