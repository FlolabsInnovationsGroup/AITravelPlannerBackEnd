package com.springboot.FlomadAIplanner.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration properties for rate limiting
 * Resolves configuration warnings identified in technical audit
 */
@Component
@ConfigurationProperties(prefix = "app.rate-limit")
public class RateLimitProperties {
    
    private int chatRequestsPerMinute = 10;
    private int chatRequestsPerHour = 100;
    private int guestRequestsPerMinute = 5;
    private int guestRequestsPerHour = 50;
    private int authRequestsPerMinute = 20;
    private int authRequestsPerHour = 200;
    private int generalRequestsPerMinute = 30;
    private int generalRequestsPerHour = 500;
    private int expensiveRequestsPerMinute = 3;
    private int expensiveRequestsPerHour = 20;
    
    // Getters and Setters
    public int getChatRequestsPerMinute() {
        return chatRequestsPerMinute;
    }
    
    public void setChatRequestsPerMinute(int chatRequestsPerMinute) {
        this.chatRequestsPerMinute = chatRequestsPerMinute;
    }
    
    public int getChatRequestsPerHour() {
        return chatRequestsPerHour;
    }
    
    public void setChatRequestsPerHour(int chatRequestsPerHour) {
        this.chatRequestsPerHour = chatRequestsPerHour;
    }
    
    public int getGuestRequestsPerMinute() {
        return guestRequestsPerMinute;
    }
    
    public void setGuestRequestsPerMinute(int guestRequestsPerMinute) {
        this.guestRequestsPerMinute = guestRequestsPerMinute;
    }
    
    public int getGuestRequestsPerHour() {
        return guestRequestsPerHour;
    }
    
    public void setGuestRequestsPerHour(int guestRequestsPerHour) {
        this.guestRequestsPerHour = guestRequestsPerHour;
    }
    
    public int getAuthRequestsPerMinute() {
        return authRequestsPerMinute;
    }
    
    public void setAuthRequestsPerMinute(int authRequestsPerMinute) {
        this.authRequestsPerMinute = authRequestsPerMinute;
    }
    
    public int getAuthRequestsPerHour() {
        return authRequestsPerHour;
    }
    
    public void setAuthRequestsPerHour(int authRequestsPerHour) {
        this.authRequestsPerHour = authRequestsPerHour;
    }
    
    public int getGeneralRequestsPerMinute() {
        return generalRequestsPerMinute;
    }
    
    public void setGeneralRequestsPerMinute(int generalRequestsPerMinute) {
        this.generalRequestsPerMinute = generalRequestsPerMinute;
    }
    
    public int getGeneralRequestsPerHour() {
        return generalRequestsPerHour;
    }
    
    public void setGeneralRequestsPerHour(int generalRequestsPerHour) {
        this.generalRequestsPerHour = generalRequestsPerHour;
    }
    
    public int getExpensiveRequestsPerMinute() {
        return expensiveRequestsPerMinute;
    }
    
    public void setExpensiveRequestsPerMinute(int expensiveRequestsPerMinute) {
        this.expensiveRequestsPerMinute = expensiveRequestsPerMinute;
    }
    
    public int getExpensiveRequestsPerHour() {
        return expensiveRequestsPerHour;
    }
    
    public void setExpensiveRequestsPerHour(int expensiveRequestsPerHour) {
        this.expensiveRequestsPerHour = expensiveRequestsPerHour;
    }
}