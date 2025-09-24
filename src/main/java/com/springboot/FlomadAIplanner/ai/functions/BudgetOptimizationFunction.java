package com.springboot.FlomadAIplanner.ai.functions;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import java.util.function.Function;

/**
 * Budget optimization function to provide cost-effective travel suggestions
 */
@Component("budgetOptimizationFunction")
@Description("Provides budget optimization suggestions for cost-effective travel planning")
@Slf4j
public class BudgetOptimizationFunction implements Function<BudgetOptimizationFunction.BudgetRequest, String> {

    @Override
    public String apply(BudgetRequest request) {
        log.info("Processing budget optimization request for destination: {}, budget: {}", 
                request.getDestination(), request.getBudget());
        
        // Mock budget optimization logic
        StringBuilder response = new StringBuilder();
        response.append("💰 Budget Optimization for ").append(request.getDestination()).append("\n\n");
        
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

    @Data
    public static class BudgetRequest {
        @JsonPropertyDescription("The destination for budget optimization")
        private String destination;
        
        @JsonPropertyDescription("The available budget in USD")
        private Double budget;
        
        @JsonPropertyDescription("The type of travel (business, leisure, family)")
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
