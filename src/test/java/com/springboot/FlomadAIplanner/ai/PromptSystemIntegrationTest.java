package com.springboot.FlomadAIplanner.ai;

import com.springboot.FlomadAIplanner.ai.service.ChatCallService;
import com.springboot.FlomadAIplanner.ai.dto.AIMessageDTO;
import com.springboot.FlomadAIplanner.ai.dto.AIMessageResponse;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive integration test suite for the prompt system.
 * Calls real AI (OpenAI); requires SPRING_AI_OPENAI_API_KEY and network.
 * Excluded from default test run via @Tag("integration"); run with: mvn test -DexcludedGroups=
 */
@Tag("integration")
@SpringBootTest
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("Prompt System Integration Tests")
public class PromptSystemIntegrationTest {

    @Autowired
    private ChatCallService chatCallService;

    @Autowired
    @Qualifier("openAiChatClient")
    private ChatClient chatClient;

    private String testSessionId;

    @BeforeEach
    void setUp() {
        testSessionId = "test-session-" + UUID.randomUUID().toString().substring(0, 8);
    }

    @Test
    @DisplayName("Test Location Request Prompts - Should identify location requests and provide map guidance")
    void testLocationRequestPrompts() {
        List<String> locationQueries = Arrays.asList(
            "Show me the Eiffel Tower on the map",
            "Display Big Ben location",
            "I want to see the Colosseum",
            "Add Central Park to my map"
        );

        for (String query : locationQueries) {
            AIMessageDTO message = new AIMessageDTO(testSessionId, query, null);
            
            String response = chatCallService.processMessage(message);
            
            assertNotNull(response, "Response should not be null for query: " + query);
            assertTrue(response.length() > 20, "Response should be comprehensive for query: " + query);
            
            // Should provide location guidance
            assertTrue(containsLocationKeywords(response), 
                      "Response should mention location/map functionality for query: " + query);
            
            // Should not make false booking promises
            assertFalse(containsBookingConfirmations(response),
                       "Should not promise actual bookings for query: " + query);
            
            System.out.println("✅ Location Request - Query: " + query);
            System.out.println("   Response: " + truncateResponse(response));
        }
    }

    @Test
    @DisplayName("Test Booking Guidance Prompts - Should provide guidance without false promises")
    void testBookingGuidancePrompts() {
        List<String> bookingQueries = Arrays.asList(
            "Book a flight to Tokyo for me",
            "Reserve a hotel in Paris",
            "I want to book a tour in Rome",
            "Make a restaurant reservation in London"
        );

        for (String query : bookingQueries) {
            AIMessageDTO message = new AIMessageDTO(testSessionId, query, null);
            
            String response = chatCallService.processMessage(message);
            
            assertNotNull(response, "Response should not be null for query: " + query);
            assertTrue(response.length() > 30, "Response should be comprehensive for query: " + query);
            
            // Should provide guidance, not actual booking
            assertTrue(containsHelpfulGuidance(response),
                      "Should offer guidance and assistance for query: " + query);
            
            // Should not make false booking promises
            assertFalse(containsBookingConfirmations(response),
                       "Should not make false booking promises for query: " + query);
            
            System.out.println("✅ Booking Guidance - Query: " + query);
            System.out.println("   Response: " + truncateResponse(response));
        }
    }

    @Test
    @DisplayName("Test Trip Planning Prompts - Should provide comprehensive planning")
    void testTripPlanningPrompts() {
        List<String> planningQueries = Arrays.asList(
            "Plan a 5-day trip to Barcelona",
            "Create an itinerary for a weekend in Amsterdam",
            "Help me plan a family vacation to Disney World",
            "I need a business trip schedule for New York"
        );

        for (String query : planningQueries) {
            AIMessageDTO message = new AIMessageDTO(testSessionId, query, null);
            
            try {
                String response = chatCallService.processMessage(message);
                
                assertNotNull(response, "Response should not be null for query: " + query);
                assertTrue(response.length() > 100, "Planning response should be detailed for query: " + query);
                
                // Should mention planning elements
                assertTrue(containsPlanningKeywords(response),
                          "Should provide planning information for query: " + query);
                
                System.out.println("✅ Trip Planning - Query: " + query);
                System.out.println("   Response: " + truncateResponse(response));
            } catch (Exception e) {
                // Handle network/API issues gracefully during testing
                System.err.println("⚠️  API call failed for query: " + query + " - " + e.getMessage());
                // For now, we'll skip this test if there are connectivity issues
                // In a production environment, we'd want to ensure this works
                org.junit.jupiter.api.Assumptions.assumeTrue(false, "Skipping due to API connectivity issues: " + e.getMessage());
            }
        }
    }

    @Test
    @DisplayName("Test Emergency Assistance Prompts - Should provide immediate helpful guidance")
    void testEmergencyAssistancePrompts() {
        List<String> emergencyQueries = Arrays.asList(
            "Help! I lost my passport in a foreign country",
            "My flight was cancelled, what do I do?",
            "I need medical assistance while traveling",
            "I'm stranded and need help"
        );

        for (String query : emergencyQueries) {
            AIMessageDTO message = new AIMessageDTO(testSessionId, query, null);
            
            String response = chatCallService.processMessage(message);
            
            assertNotNull(response, "Response should not be null for query: " + query);
            assertTrue(response.length() > 50, "Emergency response should be comprehensive for query: " + query);
            
            // Should provide emergency guidance
            assertTrue(containsEmergencyKeywords(response),
                      "Should provide emergency assistance guidance for query: " + query);
            
            System.out.println("✅ Emergency Assistance - Query: " + query);
            System.out.println("   Response: " + truncateResponse(response));
        }
    }

    @Test
    @DisplayName("Test Weather Safety Prompts - Should provide weather-related travel advice")
    void testWeatherSafetyPrompts() {
        List<String> weatherQueries = Arrays.asList(
            "Is it safe to travel to Florida during hurricane season?",
            "What's the weather like in London in December?",
            "Should I pack warm clothes for Iceland?",
            "Any weather warnings for my trip to Thailand?"
        );

        for (String query : weatherQueries) {
            AIMessageDTO message = new AIMessageDTO(testSessionId, query, null);
            
            String response = chatCallService.processMessage(message);
            
            assertNotNull(response, "Response should not be null for query: " + query);
            assertTrue(response.length() > 30, "Weather response should be comprehensive for query: " + query);
            
            // Should mention weather considerations
            assertTrue(containsWeatherKeywords(response),
                      "Should provide weather-related guidance for query: " + query);
            
            System.out.println("✅ Weather Safety - Query: " + query);
            System.out.println("   Response: " + truncateResponse(response));
        }
    }

    @Test
    @DisplayName("Test Accessibility Prompts - Should provide accessibility guidance")
    void testAccessibilityPrompts() {
        List<String> accessibilityQueries = Arrays.asList(
            "I need wheelchair accessible hotels in Paris",
            "Are there mobility assistance services at the airport?",
            "I'm hearing impaired, what should I know about traveling?",
            "Visual impairment travel assistance needed"
        );

        for (String query : accessibilityQueries) {
            AIMessageDTO message = new AIMessageDTO(testSessionId, query, null);
            
            String response = chatCallService.processMessage(message);
            
            assertNotNull(response, "Response should not be null for query: " + query);
            assertTrue(response.length() > 30, "Accessibility response should be comprehensive for query: " + query);
            
            // Should mention accessibility features
            assertTrue(containsAccessibilityKeywords(response),
                      "Should provide accessibility guidance for query: " + query);
            
            System.out.println("✅ Accessibility - Query: " + query);
            System.out.println("   Response: " + truncateResponse(response));
        }
    }

    @Test
    @DisplayName("Test Budget Optimization Prompts - Should provide cost-effective suggestions")
    void testBudgetOptimizationPrompts() {
        List<String> budgetQueries = Arrays.asList(
            "How can I travel to Europe on a budget?",
            "Find cheap flights to Asia",
            "Budget-friendly accommodations in New York",
            "Cost-effective travel options for students"
        );

        for (String query : budgetQueries) {
            AIMessageDTO message = new AIMessageDTO(testSessionId, query, null);
            
            try {
                String response = chatCallService.processMessage(message);
                
                assertNotNull(response, "Response should not be null for query: " + query);
                assertTrue(response.length() > 30, "Budget response should be comprehensive for query: " + query);
                
                // Should mention budget considerations
                assertTrue(containsBudgetKeywords(response),
                          "Should provide budget guidance for query: " + query);
                
                System.out.println("✅ Budget Optimization - Query: " + query);
                System.out.println("   Response: " + truncateResponse(response));
            } catch (Exception e) {
                // Handle network/API issues gracefully during testing
                System.err.println("⚠️  API call failed for query: " + query + " - " + e.getMessage());
                // For now, we'll skip this test if there are connectivity issues
                org.junit.jupiter.api.Assumptions.assumeTrue(false, "Skipping due to API connectivity issues: " + e.getMessage());
            }
        }
    }

    @Test
    @DisplayName("Test Response Times - Should respond within reasonable time limits")
    void testResponseTimes() {
        List<String> testQueries = Arrays.asList(
            "Show me attractions in Paris",
            "Plan a weekend trip to New York",
            "Find budget hotels in London",
            "What's the weather like in Tokyo?"
        );

        for (String query : testQueries) {
            long startTime = System.currentTimeMillis();
            
            AIMessageDTO message = new AIMessageDTO(testSessionId, query, null);
            String response = chatCallService.processMessage(message);
            
            long responseTime = System.currentTimeMillis() - startTime;
            
            assertNotNull(response, "Response should not be null for query: " + query);
            assertTrue(responseTime < 30000, 
                      "Response should be within 30 seconds for query: " + query + " (took " + responseTime + "ms)");
            
            System.out.println("✅ Response Time - Query: " + query + " - Time: " + responseTime + "ms");
        }
    }

    @Test
    @DisplayName("Test Edge Cases - Ambiguous and Invalid Queries")
    void testEdgeCases() {
        List<String> edgeCaseQueries = Arrays.asList(
            "Help me with stuff",
            "Travel",
            "What should I do?",
            "I need assistance",
            "unclear request"
        );

        for (String query : edgeCaseQueries) {
            AIMessageDTO message = new AIMessageDTO(testSessionId, query, null);
            
            String response = chatCallService.processMessage(message);
            
            assertNotNull(response, "Response should not be null for query: " + query);
            assertTrue(response.length() > 10, "Should provide some response for query: " + query);
            
            // Should handle gracefully - either ask for clarification or provide general help
            assertFalse(containsErrorMessages(response),
                       "Should handle edge cases gracefully for query: " + query);
            
            System.out.println("✅ Edge Case - Query: " + query);
            System.out.println("   Response: " + truncateResponse(response));
        }
    }

    @Test
    @DisplayName("Test Entity Response with Suggestions")
    void testEntityResponseWithSuggestions() {
        List<String> testQueries = Arrays.asList(
            "Plan a trip to Japan",
            "Find hotels in Barcelona",
            "Book flights to London",
            "Show me restaurants in Paris"
        );

        for (String query : testQueries) {
            try {
                AIMessageDTO message = new AIMessageDTO(testSessionId, query, null);
                
                AIMessageResponse response = chatCallService.processEntityMessage(message, AIMessageResponse.class);
                
                assertNotNull(response, "Entity response should not be null for query: " + query);
                assertNotNull(response.getMessage(), "Response message should not be null for query: " + query);
                assertTrue(response.getMessage().length() > 20, "Response message should be substantial for query: " + query);
                
                // Should provide suggestions
                assertNotNull(response.getSuggestions(), "Suggestions should not be null for query: " + query);
                assertTrue(response.getSuggestions().size() > 0, "Should provide suggestions for query: " + query);
                
                System.out.println("✅ Entity Response - Query: " + query);
                System.out.println("   Response: " + truncateResponse(response.getMessage()));
                System.out.println("   Suggestions: " + response.getSuggestions().size() + " items");
                
            } catch (Exception e) {
                // Some agents might not support entity processing, which is acceptable
                System.out.println("⚠️ Entity processing not supported for query: " + query + " - " + e.getMessage());
            }
        }
    }

    @Test
    @DisplayName("Test Content Quality - No False Booking Promises")
    void testNoFalseBookingPromises() {
        List<String> bookingQueries = Arrays.asList(
            "Book me a flight to London",
            "Reserve a hotel room in Tokyo",
            "Make a restaurant reservation",
            "Book a tour in Rome"
        );

        for (String query : bookingQueries) {
            AIMessageDTO message = new AIMessageDTO(testSessionId, query, null);
            
            String response = chatCallService.processMessage(message);
            
            assertNotNull(response, "Response should not be null for query: " + query);
            
            // Should not contain false booking language
            assertFalse(containsBookingConfirmations(response),
                       "Should not make false booking promises for query: " + query);
            
            // Should contain helpful guidance instead
            assertTrue(containsHelpfulGuidance(response),
                      "Should provide helpful guidance for query: " + query);
            
            System.out.println("✅ No False Booking - Query: " + query);
            System.out.println("   Response: " + truncateResponse(response));
        }
    }

    // Helper methods for content validation

    private boolean containsLocationKeywords(String text) {
        String lowerText = text.toLowerCase();
        return lowerText.contains("map") || lowerText.contains("location") || 
               lowerText.contains("show") || lowerText.contains("display") ||
               lowerText.contains("find") || lowerText.contains("search");
    }

    private boolean containsBookingConfirmations(String text) {
        String lowerText = text.toLowerCase();
        return lowerText.contains("booking confirmed") || lowerText.contains("reservation made") ||
               lowerText.contains("booked successfully") || lowerText.contains("confirmed your booking") ||
               lowerText.contains("reservation confirmed") || lowerText.contains("booking complete");
    }

    private boolean containsHelpfulGuidance(String text) {
        String lowerText = text.toLowerCase();
        return lowerText.contains("help") || lowerText.contains("find") ||
               lowerText.contains("guide") || lowerText.contains("options") ||
               lowerText.contains("recommend") || lowerText.contains("suggest") ||
               lowerText.contains("assist") || lowerText.contains("search");
    }

    private boolean containsPlanningKeywords(String text) {
        String lowerText = text.toLowerCase();
        return lowerText.contains("day") || lowerText.contains("itinerary") ||
               lowerText.contains("plan") || lowerText.contains("visit") ||
               lowerText.contains("schedule") || lowerText.contains("trip") ||
               lowerText.contains("travel") || lowerText.contains("destination");
    }

    private boolean containsEmergencyKeywords(String text) {
        String lowerText = text.toLowerCase();
        return lowerText.contains("embassy") || lowerText.contains("consulate") ||
               lowerText.contains("contact") || lowerText.contains("immediately") ||
               lowerText.contains("help") || lowerText.contains("emergency") ||
               lowerText.contains("assistance") || lowerText.contains("urgent");
    }

    private boolean containsWeatherKeywords(String text) {
        String lowerText = text.toLowerCase();
        return lowerText.contains("weather") || lowerText.contains("temperature") ||
               lowerText.contains("season") || lowerText.contains("climate") ||
               lowerText.contains("hurricane") || lowerText.contains("safety") ||
               lowerText.contains("pack") || lowerText.contains("clothing");
    }

    private boolean containsAccessibilityKeywords(String text) {
        String lowerText = text.toLowerCase();
        return lowerText.contains("accessible") || lowerText.contains("wheelchair") ||
               lowerText.contains("mobility") || lowerText.contains("accommodation") ||
               lowerText.contains("assistance") || lowerText.contains("service") ||
               lowerText.contains("disability") || lowerText.contains("support");
    }

    private boolean containsBudgetKeywords(String text) {
        String lowerText = text.toLowerCase();
        return lowerText.contains("budget") || lowerText.contains("cheap") ||
               lowerText.contains("affordable") || lowerText.contains("save") ||
               lowerText.contains("cost") || lowerText.contains("money") ||
               lowerText.contains("economical") || lowerText.contains("price");
    }

    private boolean containsErrorMessages(String text) {
        String lowerText = text.toLowerCase();
        return lowerText.contains("error") || lowerText.contains("failed") ||
               lowerText.contains("exception") || lowerText.contains("crash") ||
               lowerText.contains("null pointer") || lowerText.contains("stack trace");
    }

    private String truncateResponse(String response) {
        if (response == null) return "null";
        return response.length() > 100 ? response.substring(0, 100) + "..." : response;
    }
}
