package com.springboot.FlomadAIplanner.ai.functions;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.databind.JsonNode;
import com.springboot.FlomadAIplanner.dto.google.TextSearchRequest;
import com.springboot.FlomadAIplanner.service.google.GooglePlacesServiceAsync;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

/**
 * Enhanced budget optimization function with REAL venue data from Google Places.
 * Provides specific, budget-appropriate recommendations instead of generic tips.
 * 
 * @author AI Travel Planner Team
 * @since December 2025
 */
@Component("budgetOptimizationFunction")
@Description("""
        💰 SMART BUDGET OPTIMIZER - Find the best value experiences at your destination
        
        Provides SPECIFIC budget-optimized recommendations with REAL venues, prices, and cost-saving strategies.
        Uses live Google Places data to suggest actual restaurants, attractions, and activities that fit your budget.
        
        ✨ BUDGET-SMART FEATURES:
        - Real venue recommendations within budget constraints
        - Specific price levels and cost estimates
        - Free and low-cost alternatives to tourist traps
        - Money-saving booking strategies with timing
        - Local favorites that offer better value
        
        🎯 PERFECT FOR BUDGET TRAVELERS:
        ✅ "Find budget-friendly restaurants in Paris under €20/person"
        ✅ "Suggest free activities in London"
        ✅ "Optimize my $100/day Tokyo budget"
        ✅ "Find cheap eats in Rome that locals love"
        
        Returns: Specific venue recommendations with prices, plus strategic money-saving tips
        """)
@Slf4j
public class BudgetOptimizationFunction implements Function<BudgetOptimizationFunction.BudgetRequest, String> {

    private final GooglePlacesServiceAsync googlePlacesService;

    public BudgetOptimizationFunction(GooglePlacesServiceAsync googlePlacesService) {
        this.googlePlacesService = googlePlacesService;
        log.info("💰 BudgetOptimizationFunction initialized with real venue data integration");
    }

    @Override
    public String apply(BudgetRequest request) {
        log.info("💰 Processing budget optimization: destination={}, budget=${}, type={}", 
                request.getDestination(), request.getBudget(), request.getTravelType());
        
        StringBuilder response = new StringBuilder();
        response.append("💰 BUDGET OPTIMIZATION FOR ").append(request.getDestination().toUpperCase()).append("\n\n");
        
        try {
            // Fetch real budget-friendly venues
            List<JsonNode> budgetVenues = fetchBudgetFriendlyVenues(request);
            List<JsonNode> freeAttractions = fetchFreeAttractions(request);
            
            // Generate budget tier recommendations
            if (request.getBudget() != null) {
                response.append(generateBudgetTierAdvice(request.getBudget()));
            }
            
            // Add specific venue recommendations
            if (!budgetVenues.isEmpty()) {
                response.append("\n🍽️ BUDGET-FRIENDLY DINING OPTIONS:\n");
                response.append(formatBudgetVenues(budgetVenues, "dining"));
            }
            
            if (!freeAttractions.isEmpty()) {
                response.append("\n🎯 FREE & LOW-COST ATTRACTIONS:\n");
                response.append(formatBudgetVenues(freeAttractions, "attractions"));
            }
            
            // Add strategic money-saving tips
            response.append(generateStrategicSavingsTips(request.getDestination(), request.getBudget()));
            
        } catch (Exception e) {
            log.error("Error fetching budget venues: {}", e.getMessage());
            response.append(generateGenericBudgetAdvice(request));
        }
        
        return response.toString();
    }

    /**
     * Fetches budget-friendly venues from Google Places
     */
    private List<JsonNode> fetchBudgetFriendlyVenues(BudgetRequest request) {
        List<JsonNode> venues = new ArrayList<>();
        
        try {
            // Search for budget-friendly restaurants
            TextSearchRequest searchRequest = new TextSearchRequest();
            searchRequest.setTextQuery("budget restaurant local favorite in " + request.getDestination());
            
            CompletableFuture<JsonNode> searchFuture = googlePlacesService.asyncTextSearchPlaces(searchRequest);
            JsonNode result = searchFuture.join();
            
            JsonNode places = result.get("places");
            if (places != null && places.isArray()) {
                for (JsonNode place : places) {
                    // Filter by price level
                    String priceLevel = place.has("priceLevel") ? place.get("priceLevel").asText() : null;
                    if (isBudgetFriendly(priceLevel, request.getBudget())) {
                        venues.add(place);
                        if (venues.size() >= 5) break;
                    }
                }
            }
            
        } catch (Exception e) {
            log.error("Error fetching budget-friendly venues: {}", e.getMessage());
        }
        
        return venues;
    }

    /**
     * Fetches free attractions and activities
     */
    private List<JsonNode> fetchFreeAttractions(BudgetRequest request) {
        List<JsonNode> attractions = new ArrayList<>();
        
        try {
            TextSearchRequest searchRequest = new TextSearchRequest();
            searchRequest.setTextQuery("free attractions parks museums in " + request.getDestination());
            
            CompletableFuture<JsonNode> searchFuture = googlePlacesService.asyncTextSearchPlaces(searchRequest);
            JsonNode result = searchFuture.join();
            
            JsonNode places = result.get("places");
            if (places != null && places.isArray()) {
                for (JsonNode place : places) {
                    String priceLevel = place.has("priceLevel") ? place.get("priceLevel").asText() : null;
                    if (priceLevel == null || priceLevel.equals("PRICE_LEVEL_FREE") || priceLevel.equals("PRICE_LEVEL_INEXPENSIVE")) {
                        attractions.add(place);
                        if (attractions.size() >= 5) break;
                    }
                }
            }
            
        } catch (Exception e) {
            log.error("Error fetching free attractions: {}", e.getMessage());
        }
        
        return attractions;
    }

    /**
     * Checks if a price level fits the budget
     */
    private boolean isBudgetFriendly(String priceLevel, Double budget) {
        if (priceLevel == null) return true; // Unknown price, include it
        
        if (budget == null) {
            // If no budget specified, include inexpensive and moderate
            return priceLevel.equals("PRICE_LEVEL_FREE") || 
                   priceLevel.equals("PRICE_LEVEL_INEXPENSIVE") ||
                   priceLevel.equals("PRICE_LEVEL_MODERATE");
        }
        
        // Budget tiers
        if (budget < 50) {
            return priceLevel.equals("PRICE_LEVEL_FREE") || priceLevel.equals("PRICE_LEVEL_INEXPENSIVE");
        } else if (budget < 100) {
            return !priceLevel.equals("PRICE_LEVEL_VERY_EXPENSIVE");
        }
        
        return true; // Higher budgets can include all options
    }

    /**
     * Generates budget tier-specific advice
     */
    private String generateBudgetTierAdvice(Double budget) {
        StringBuilder advice = new StringBuilder();
        
        if (budget < 50) {
            advice.append("🏷️ ULTRA-BUDGET STRATEGY (Under $50/day):\n");
            advice.append("• Focus on street food and local markets (save 50-70% vs restaurants)\n");
            advice.append("• Free walking tours and self-guided exploration\n");
            advice.append("• Hostels with free breakfast (~$20-30/night)\n");
            advice.append("• Public transportation day passes\n");
            advice.append("• Picnic lunches in parks (grocery stores = massive savings!)\n");
        } else if (budget < 100) {
            advice.append("💡 SMART BUDGET STRATEGY ($50-100/day):\n");
            advice.append("• Mix of budget hotels and mid-range stays\n");
            advice.append("• Lunch menus at nice restaurants (40% cheaper than dinner!)\n");
            advice.append("• Free museum days (most cities have them!)\n");
            advice.append("• Local SIM card for navigation and deals\n");
            advice.append("• Breakfast at accommodations, street food for lunch\n");
        } else if (budget < 200) {
            advice.append("✨ COMFORTABLE BUDGET ($100-200/day):\n");
            advice.append("• Quality mid-range hotels in good locations\n");
            advice.append("• Skip-the-line tickets for efficiency\n");
            advice.append("• Nice dinners without breaking the bank\n");
            advice.append("• Guided tours for deep cultural experiences\n");
            advice.append("• Occasional splurges on signature experiences\n");
        } else {
            advice.append("🌟 PREMIUM BUDGET ($200+/day):\n");
            advice.append("• Boutique hotels and unique stays\n");
            advice.append("• Fine dining at local favorites\n");
            advice.append("• Private guides and exclusive experiences\n");
            advice.append("• Still focus on VALUE - avoid tourist traps!\n");
            advice.append("• Invest in unforgettable signature activities\n");
        }
        
        return advice.toString();
    }

    /**
     * Formats budget venue recommendations
     */
    private String formatBudgetVenues(List<JsonNode> venues, String category) {
        StringBuilder formatted = new StringBuilder();
        int count = 1;
        
        for (JsonNode venue : venues) {
            String name = extractDisplayName(venue);
            String address = extractAddress(venue);
            Double rating = extractRating(venue);
            String priceLevel = extractPriceLevel(venue);
            
            formatted.append("\n").append(count).append(". ✨ ").append(name).append("\n");
            
            if (address != null && !address.isEmpty()) {
                formatted.append("   📍 ").append(address).append("\n");
            }
            
            if (rating != null) {
                formatted.append("   ⭐ ").append(String.format("%.1f", rating)).append(" rating\n");
            }
            
            if (priceLevel != null) {
                formatted.append("   💰 ").append(formatPriceLevelForBudget(priceLevel)).append("\n");
            }
            
            // Add money-saving tip
            formatted.append("   💡 ").append(generateBudgetTip(category, priceLevel)).append("\n");
            
            count++;
        }
        
        return formatted.toString();
    }

    /**
     * Generates strategic money-saving tips specific to the destination
     */
    private String generateStrategicSavingsTips(String destination, Double budget) {
        StringBuilder tips = new StringBuilder();
        
        tips.append("\n📊 STRATEGIC MONEY-SAVING TIPS:\n\n");
        
        tips.append("🕐 TIMING HACKS:\n");
        tips.append("• Book flights 6-8 weeks ahead (save 25-40%)\n");
        tips.append("• Visit during shoulder season (30-50% cheaper)\n");
        tips.append("• Museums often free on first Sunday/Wednesday\n");
        tips.append("• Dinner early (5-6pm) for happy hour prices\n\n");
        
        tips.append("🎟️ BOOKING STRATEGIES:\n");
        tips.append("• Multi-attraction passes = 40-60% savings\n");
        tips.append("• Book direct with venues (no booking fees)\n");
        tips.append("• Student/senior discounts widely available\n");
        tips.append("• Group tickets for 3+ people = 10-20% off\n\n");
        
        tips.append("🚇 TRANSPORTATION SAVINGS:\n");
        tips.append("• Multi-day transit passes (daily cost drops 40%)\n");
        tips.append("• Walk between nearby attractions (free + exercise!)\n");
        tips.append("• Bike rentals cheaper than taxis (and more fun)\n");
        tips.append("• Airport public transport vs taxi = save $30-50\n\n");
        
        tips.append("🍽️ DINING COST HACKS:\n");
        tips.append("• Lunch menus = same food, 40% less cost\n");
        tips.append("• Local markets for breakfast/snacks\n");
        tips.append("• 'Menu of the day' or 'prix fixe' = best value\n");
        tips.append("• Tap water is free (save $5-8/meal)\n");
        tips.append("• Picnic dinners = local experience + savings\n\n");
        
        return tips.toString();
    }

    /**
     * Generates fallback generic budget advice
     */
    private String generateGenericBudgetAdvice(BudgetRequest request) {
        StringBuilder response = new StringBuilder();
        
        if (request.getBudget() != null && request.getBudget() < 1000) {
            response.append("🏷️ LOW BUDGET TIPS:\n");
            response.append("• Book flights 6-8 weeks in advance for 25% savings\n");
            response.append("• Consider hostels or budget hotels (save 60-70%)\n");
            response.append("• Use public transportation instead of taxis\n");
            response.append("• Eat at local markets and street food vendors\n");
            response.append("• Look for free walking tours and activities\n");
        } else if (request.getBudget() != null && request.getBudget() < 3000) {
            response.append("💡 MODERATE BUDGET OPTIMIZATION:\n");
            response.append("• Mix of budget and mid-range accommodations\n");
            response.append("• Book direct flights to save on connections\n");
            response.append("• Use travel reward credit cards for points\n");
            response.append("• Book activities in advance for early bird discounts\n");
        } else {
            response.append("✨ PREMIUM BUDGET OPTIMIZATION:\n");
            response.append("• Focus on value-for-money premium experiences\n");
            response.append("• Consider business class upgrades during sales\n");
            response.append("• Book luxury hotels during shoulder seasons\n");
            response.append("• Invest in skip-the-line tours and private guides\n");
        }
        
        response.append("\n📊 GENERAL MONEY-SAVING STRATEGIES:\n");
        response.append("• Travel during off-peak seasons (save 30-50%)\n");
        response.append("• Use flexible date searches for flights\n");
        response.append("• Compare prices across multiple booking platforms\n");
        response.append("• Consider travel insurance to protect your investment\n");
        response.append("• Download local apps for discounts and deals\n");
        
        return response.toString();
    }

    /**
     * Generates budget-specific tips for venues
     */
    private String generateBudgetTip(String category, String priceLevel) {
        if ("dining".equals(category)) {
            if ("PRICE_LEVEL_INEXPENSIVE".equals(priceLevel)) {
                return "Local favorite! Great value for authentic experience";
            } else if ("PRICE_LEVEL_FREE".equals(priceLevel) || priceLevel == null) {
                return "Perfect budget choice - doesn't compromise on quality!";
            }
            return "Try lunch menu for 30-40% savings vs dinner";
        } else {
            if ("PRICE_LEVEL_FREE".equals(priceLevel)) {
                return "Completely free - check hours to plan your visit";
            }
            return "Check for student/senior discounts at entrance";
        }
    }

    /**
     * Formats price level for budget display
     */
    private String formatPriceLevelForBudget(String priceLevel) {
        switch (priceLevel) {
            case "PRICE_LEVEL_FREE": return "FREE! 🎉";
            case "PRICE_LEVEL_INEXPENSIVE": return "$ - Budget-friendly (~$5-15)";
            case "PRICE_LEVEL_MODERATE": return "$$ - Moderate value (~$15-30)";
            case "PRICE_LEVEL_EXPENSIVE": return "$$$ - Splurge-worthy (~$30-50)";
            case "PRICE_LEVEL_VERY_EXPENSIVE": return "$$$$ - Special occasion (~$50+)";
            default: return "Price varies";
        }
    }

    // Helper methods for data extraction
    
    private String extractDisplayName(JsonNode place) {
        JsonNode displayName = place.get("displayName");
        if (displayName != null && displayName.has("text")) {
            return displayName.get("text").asText();
        }
        return "Local Venue";
    }
    
    private String extractAddress(JsonNode place) {
        JsonNode address = place.get("formattedAddress");
        return address != null ? address.asText() : null;
    }
    
    private Double extractRating(JsonNode place) {
        JsonNode rating = place.get("rating");
        return rating != null ? rating.asDouble() : null;
    }
    
    private String extractPriceLevel(JsonNode place) {
        JsonNode priceLevel = place.get("priceLevel");
        return priceLevel != null ? priceLevel.asText() : null;
    }

    @Data
    public static class BudgetRequest {
        @JsonPropertyDescription("The destination for budget optimization (city, country)")
        private String destination;
        
        @JsonPropertyDescription("The available daily budget in USD")
        private Double budget;
        
        @JsonPropertyDescription("The type of travel (business, leisure, family, solo, couple)")
        private String travelType;
        
        // No-args constructor for Jackson
        public BudgetRequest() {}
        
        // Parameterized constructor
        public BudgetRequest(String destination, Double budget, String travelType) {
            this.destination = destination;
            this.budget = budget;
            this.travelType = travelType;
        }
    }
}
