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
    
    // Track sessions with paused broadcasting to prevent race conditions
    private final Map<String, Boolean> pausedSessions = new HashMap<>();

    public WebSocketService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void sendLocationUpdate(Location location) {
        if (location == null || location.getSessionId() == null) {
            return;
        }
        
        // Check if broadcasting is paused for this session (race condition prevention)
        if (isBroadcastingPaused(location.getSessionId())) {
            return;
        }
        
        String destination = LOCATION_DESTINATION + location.getSessionId();

        Map<String, Object> message = createMessage("LOCATION_ADDED", location);

        try {
            messagingTemplate.convertAndSend(destination, message);
        } catch (Exception e) {
            log.error("Failed to send LOCATION_ADDED: {}", e.getMessage());
            retryMessage(destination, message, 1, 3);
        }
    }

    public void sendMapCleared(String sessionId) {
        if (!isValidSessionId(sessionId)) {
            return;
        }
        String destination = LOCATION_DESTINATION + sessionId;

        Map<String, Object> message = createMessage("MAP_CLEARED", new HashMap<>());

        try {
            messagingTemplate.convertAndSend(destination, message);
        } catch (Exception e) {
            log.error("Failed to send MAP_CLEARED: {}", e.getMessage());
            retryMessage(destination, message, 1, 3);
        }
    }

    public void sendLocationRemoved(String sessionId, String locationId) {
        if (!isValidSessionId(sessionId) || locationId == null || locationId.trim().isEmpty()) {
            return;
        }
        String destination = LOCATION_DESTINATION + sessionId;

        Map<String, String> payload = new HashMap<>();
        payload.put("locationId", locationId);

        Map<String, Object> message = createMessage("LOCATION_REMOVED", payload);

        try {
            messagingTemplate.convertAndSend(destination, message);
        } catch (Exception e) {
            log.error("Failed to send LOCATION_REMOVED: {}", e.getMessage());
            retryMessage(destination, message, 1, 3);
        }
    }

    /**
     * Pause WebSocket broadcasting for a session to prevent race conditions during clearing operations
     */
    public void pauseBroadcasting(String sessionId) {
        if (isValidSessionId(sessionId)) {
            pausedSessions.put(sessionId, true);
            log.info("🚫 Paused broadcasting for sessionId: {}", sessionId);
        }
    }

    /**
     * Resume WebSocket broadcasting for a session after clearing operations complete
     */
    public void resumeBroadcasting(String sessionId) {
        if (isValidSessionId(sessionId)) {
            pausedSessions.remove(sessionId);
            log.info("✅ Resumed broadcasting for sessionId: {}", sessionId);
        }
    }

    /**
     * Check if broadcasting is paused for a session
     */
    private boolean isBroadcastingPaused(String sessionId) {
        return pausedSessions.getOrDefault(sessionId, false);
    }

    // Retry mechanism for failed WebSocket messages
    private void retryMessage(String destination, Map<String, Object> message, int attempt, int maxAttempts) {
        if (attempt > maxAttempts) {
            log.error("Failed to send WebSocket message after {} attempts to destination: {}", maxAttempts, destination);
            return;
        }

        // Exponential backoff: 1s, 2s, 4s...
        long delay = (long) Math.pow(2, attempt - 1) * 1000;
        
        log.info("Retrying WebSocket message to {} in {}ms (attempt {}/{})", destination, delay, attempt, maxAttempts);
        
        // Use a separate thread for retry to avoid blocking
        new Thread(() -> {
            try {
                Thread.sleep(delay);
                messagingTemplate.convertAndSend(destination, message);
                log.info("WebSocket message retry successful for destination: {} on attempt {}", destination, attempt);
            } catch (Exception e) {
                log.warn("WebSocket message retry failed for destination: {} on attempt {}: {}", destination, attempt, e.getMessage());
                retryMessage(destination, message, attempt + 1, maxAttempts);
            }
        }).start();
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

