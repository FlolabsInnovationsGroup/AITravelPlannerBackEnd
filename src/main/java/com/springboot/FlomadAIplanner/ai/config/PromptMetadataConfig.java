package com.springboot.FlomadAIplanner.ai.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;
import java.util.HashMap;

/**
 * Configuration for lightweight prompt metadata used in prompt selection.
 * This avoids sending full prompt content to OpenAI for selection.
 */
@Configuration
public class PromptMetadataConfig {

    /**
     * Creates a map of prompt filenames to their descriptions for efficient selection.
     * This replaces sending full prompt content during the selection phase.
     * 
     * @return Map of prompt filename to description
     */
    @Bean(name = "promptMetadata")
    public Map<String, String> promptMetadata() {
        Map<String, String> metadata = new HashMap<>();
        
        // Emergency & High Priority
        metadata.put("EmergencyAssistancePrompt.txt", 
            "Emergency travel assistance for stranded, lost, medical emergencies, cancelled flights, urgent help");
        
        metadata.put("BudgetOptimizationPrompt.txt", 
            "Budget travel optimization for cheap flights, affordable hotels, cost-saving, economical travel");
        
        // Location & Booking
        metadata.put("LocationRequestPrompt.txt", 
            "Simple location display requests like 'show me Paris', 'display Eiffel Tower'");
        
        metadata.put("BookingConfirmationPrompt.txt", 
            "Flight/hotel/tour searches and booking guidance without budget concerns");
        
        metadata.put("UnifiedPlanningPrompt.txt", 
            "Comprehensive trip planning, detailed itineraries, multi-day trips, activity recommendations");
        
        // Specialized Travel Needs
        metadata.put("AccessibilityPrompt.txt", 
            "Wheelchair accessible travel, hearing impaired, mobility assistance, special needs");
        
        metadata.put("TravelDocumentPrompt.txt", 
            "Visa requirements, passport issues, entry requirements, travel documents");
        
        metadata.put("MultiLanguagePrompt.txt", 
            "Language translation, cultural customs, communication support, local phrases");
        
        metadata.put("GroupTravelPrompt.txt", 
            "Family trips, group bookings, traveling with friends, multiple travelers");
        
        metadata.put("WeatherSafetyPrompt.txt", 
            "Weather conditions, travel safety, seasonal advice, climate concerns");
        
        // Changes & Modifications
        metadata.put("ItineraryModificationPrompt.txt", 
            "Trip changes, modify dates, cancel reservations, itinerary updates");
        
        metadata.put("LocalRegulationsPrompt.txt", 
            "Customs rules, local laws, legal compliance, what to bring/avoid");
        
        // General Questions
        metadata.put("SingleQuestionPrompt.txt", 
            "Simple travel questions, general advice, basic inquiries without functions");
        
        metadata.put("WithFunctionsQuestionPrompt.txt", 
            "Questions requiring function calls like weather, search, or data retrieval");
        
        metadata.put("WebSearchPrompt.txt", 
            "Internet search required for latest information, real-time data, current events");
        
        return metadata;
    }
}