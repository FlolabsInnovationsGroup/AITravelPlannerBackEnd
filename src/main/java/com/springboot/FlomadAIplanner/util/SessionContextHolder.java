package com.springboot.FlomadAIplanner.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Utility class to hold the current session context in a thread-local variable.
 * This allows AI functions to access the current session ID without requiring it as a parameter.
 */
@Slf4j
@Component
public class SessionContextHolder {
    
    private static final ThreadLocal<String> sessionIdHolder = new ThreadLocal<>();
    
    /**
     * Sets the session ID for the current thread.
     * 
     * @param sessionId the session ID to set
     */
    public static void setSessionId(String sessionId) {
        log.debug("Setting session ID in context: {}", sessionId);
        sessionIdHolder.set(sessionId);
    }
    
    /**
     * Gets the session ID for the current thread.
     * 
     * @return the session ID or null if not set
     */
    public static String getSessionId() {
        String sessionId = sessionIdHolder.get();
        log.debug("Retrieved session ID from context: {}", sessionId);
        return sessionId;
    }
    
    /**
     * Clears the session ID for the current thread.
     */
    public static void clear() {
        String sessionId = sessionIdHolder.get();
        log.debug("Clearing session ID from context: {}", sessionId);
        sessionIdHolder.remove();
    }
}
