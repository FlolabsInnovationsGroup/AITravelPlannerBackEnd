package com.springboot.FlomadAIplanner.service.socket;

import com.springboot.FlomadAIplanner.model.entities.Location;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public final class WebSocketService {

    /** Base destination for location updates. */
    private static final String LOCATION_DESTINATION = "/topic/locations/";
    /** Max retry attempts for failed sends. */
    private static final int MAX_RETRY_ATTEMPTS = 3;
    /** Base delay in ms for retry backoff. */
    private static final long RETRY_DELAY_MS = 1000;

    /** Messaging template for STOMP. */
    private final SimpMessagingTemplate messagingTemplate;

    /** Sessions with paused broadcasting (race condition prevention). */
    private final Map<String, Boolean> pausedSessions = new HashMap<>();

    /**
     * Constructor.
     *
     * @param pMessagingTemplate SimpMessagingTemplate
     */
    public WebSocketService(final SimpMessagingTemplate pMessagingTemplate) {
        this.messagingTemplate = pMessagingTemplate;
    }

    /**
     * Sends a location update to the session topic.
     *
     * @param location location entity
     */
    public void sendLocationUpdate(final Location location) {
        if (location == null || location.getSessionId() == null) {
            log.warn("Invalid location or sessionId for LOCATION_ADDED event.");
            return;
        }

        if (isBroadcastingPaused(location.getSessionId())) {
            log.info("Skipping LOCATION_ADDED broadcast sessionId {} (paused)",
                    location.getSessionId());
            return;
        }

        String destination = LOCATION_DESTINATION + location.getSessionId();
        log.info("Sending LOCATION_ADDED to destination: {}", destination);

        Map<String, Object> message = createMessage("LOCATION_ADDED", location);

        try {
            messagingTemplate.convertAndSend(destination, message);
            log.debug("LOCATION_ADDED message sent successfully to session: {}",
                    location.getSessionId());
        } catch (Exception e) {
            log.error("LOCATION_ADDED send failed sessionId: {} dest: {}: {}",
                    location.getSessionId(), destination, e.getMessage(), e);

            retryMessage(destination, message, 1, MAX_RETRY_ATTEMPTS);
        }
    }

    /**
     * Notifies session that map was cleared.
     *
     * @param sessionId session id
     */
    public void sendMapCleared(final String sessionId) {
        if (!isValidSessionId(sessionId)) {
            log.warn("Invalid sessionId provided for MAP_CLEARED event.");
            return;
        }
        String destination = LOCATION_DESTINATION + sessionId;

        log.info("Sending MAP_CLEARED to destination: {}", destination);

        Map<String, Object> message =
                createMessage("MAP_CLEARED", new HashMap<>());

        try {
            messagingTemplate.convertAndSend(destination, message);
            log.debug("MAP_CLEARED sent to session: {}", sessionId);
        } catch (Exception e) {
            log.error("MAP_CLEARED send failed sessionId: {} dest: {}: {}",
                    sessionId, destination, e.getMessage(), e);

            retryMessage(destination, message, 1, MAX_RETRY_ATTEMPTS);
        }
    }

    /**
     * Notifies session that a location was removed.
     *
     * @param sessionId session id
     * @param locationId location id
     */
    public void sendLocationRemoved(final String sessionId,
            final String locationId) {
        boolean invalid = !isValidSessionId(sessionId)
                || locationId == null
                || locationId.trim().isEmpty();
        if (invalid) {
            log.warn("Invalid sessionId/locationId for LOCATION_REMOVED.");
            return;
        }
        String destination = LOCATION_DESTINATION + sessionId;
        log.info("Sending LOCATION_REMOVED to destination: {}", destination);

        Map<String, String> payload = new HashMap<>();
        payload.put("locationId", locationId);

        Map<String, Object> message =
                createMessage("LOCATION_REMOVED", payload);

        try {
            messagingTemplate.convertAndSend(destination, message);
            log.debug("LOCATION_REMOVED sent to session: {}", sessionId);
        } catch (Exception e) {
            log.error("LOCATION_REMOVED send failed {} {} dest: {}: {}",
                    sessionId, locationId, destination, e.getMessage(), e);

            retryMessage(destination, message, 1, MAX_RETRY_ATTEMPTS);
        }
    }

    /**
     * Pauses WebSocket broadcasting for a session (race condition prevention).
     *
     * @param sessionId session id
     */
    public void pauseBroadcasting(final String sessionId) {
        if (isValidSessionId(sessionId)) {
            pausedSessions.put(sessionId, true);
            log.info("🚫 Paused broadcasting for sessionId: {}", sessionId);
        }
    }

    /**
     * Resumes WebSocket broadcasting for a session.
     *
     * @param sessionId session id
     */
    public void resumeBroadcasting(final String sessionId) {
        if (isValidSessionId(sessionId)) {
            pausedSessions.remove(sessionId);
            log.info("✅ Resumed broadcasting for sessionId: {}", sessionId);
        }
    }

    /**
     * Checks if broadcasting is paused for a session.
     *
     * @param sessionId session id
     * @return true if paused
     */
    private boolean isBroadcastingPaused(final String sessionId) {
        return pausedSessions.getOrDefault(sessionId, false);
    }

    private void retryMessage(
            final String destination,
            final Map<String, Object> message,
            final int attempt,
            final int maxAttempts) {
        if (attempt > maxAttempts) {
            log.error("WebSocket send failed after {} attempts to {}",
                    maxAttempts, destination);
            return;
        }

        long delay = (long) Math.pow(2, attempt - 1) * RETRY_DELAY_MS;

        log.info("Retrying WebSocket message to {} in {}ms (attempt {}/{})",
                destination, delay, attempt, maxAttempts);

        new Thread(() -> {
            try {
                Thread.sleep(delay);
                messagingTemplate.convertAndSend(destination, message);
                log.info("WebSocket retry successful for dest: {} attempt {}",
                        destination, attempt);
            } catch (Exception e) {
                log.warn("WebSocket retry failed {} attempt {}: {}",
                        destination, attempt, e.getMessage());
                retryMessage(destination, message, attempt + 1, maxAttempts);
            }
        }).start();
    }

    private Map<String, Object> createMessage(final String type,
            final Object payload) {
        Map<String, Object> message = new HashMap<>();
        message.put("type", type);
        message.put("payload", payload);
        return message;
    }

    /**
     * Validates session id non-null and non-empty.
     *
     * @param sessionId session id
     * @return true if valid
     */
    private boolean isValidSessionId(final String sessionId) {
        return sessionId != null && !sessionId.trim().isEmpty();
    }
}
