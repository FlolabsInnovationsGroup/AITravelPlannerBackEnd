package com.springboot.FlomadAIplanner.service.google;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
// import org.springframework.scheduling.annotation.Scheduled; // TEMPORARILY DISABLED - causing circular dependency
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Service to track Google Places API usage and costs.
 * Provides daily reporting and cost alerts to prevent billing surprises.
 */
@Slf4j
@Service
public class GooglePlacesMetricsService {

    // Google Places API pricing (as of December 2025)
    private static final double COST_PER_TEXT_SEARCH = 0.02012;
    private static final double COST_PER_PLACE_DETAILS = 0.00805;
    private static final double COST_PER_ATMOSPHERE_DATA = 0.00447; // Eliminated by config change

    @Value("${google.places.daily-cost-threshold:10.00}")
    private double dailyCostThreshold;

    @Value("${google.places.monthly-cost-threshold:100.00}")
    private double monthlyCostThreshold;

    // CIRCULAR DEPENDENCY FIX: Removed @Autowired GooglePlacesServiceAsync
    // This was causing: GooglePlacesMetricsService -> GooglePlacesServiceAsync -> GooglePlacesMetricsService
    // Cache metrics reporting is now disabled to break the circular reference
    
    // Daily counters (reset at midnight)
    private final AtomicLong dailyTextSearchCalls = new AtomicLong(0);
    private final AtomicLong dailyPlaceDetailsCalls = new AtomicLong(0);

    // Monthly counters (reset on 1st of month)
    private final AtomicLong monthlyTextSearchCalls = new AtomicLong(0);
    private final AtomicLong monthlyPlaceDetailsCalls = new AtomicLong(0);

    /**
     * Record a text search API call.
     */
    public void recordTextSearch() {
        dailyTextSearchCalls.incrementAndGet();
        monthlyTextSearchCalls.incrementAndGet();
    }

    /**
     * Record a place details API call.
     */
    public void recordPlaceDetails() {
        dailyPlaceDetailsCalls.incrementAndGet();
        monthlyPlaceDetailsCalls.incrementAndGet();
    }

    /**
     * Log daily usage and cost metrics every 24 hours at midnight.
     * TEMPORARILY DISABLED - re-enable after fixing circular dependency
     */
    // @Scheduled(cron = "0 0 0 * * *") // Run at midnight
    public void reportDailyMetrics() {
        long textSearchCount = dailyTextSearchCalls.getAndSet(0);
        long placeDetailsCount = dailyPlaceDetailsCalls.getAndSet(0);

        double textSearchCost = textSearchCount * COST_PER_TEXT_SEARCH;
        double placeDetailsCost = placeDetailsCount * COST_PER_PLACE_DETAILS;
        double totalDailyCost = textSearchCost + placeDetailsCost;

        // Cache metrics disabled due to circular dependency fix
        String cacheStats = "Cache metrics temporarily disabled";

        log.info("╔═══════════════════════════════════════════════════════════════╗");
        log.info("║     DAILY GOOGLE PLACES API COST REPORT                       ║");
        log.info("╠═══════════════════════════════════════════════════════════════╣");
        log.info("║ 📡 API Calls:                                                 ║");
        log.info("║    • Text Search (Enterprise): {} calls → ${}", 
                String.format("%-8d", textSearchCount), String.format("%8.2f", textSearchCost));
        log.info("║    • Place Details: {} calls → ${}", 
                String.format("%-15d", placeDetailsCount), String.format("%8.2f", placeDetailsCost));
        log.info("╠═══════════════════════════════════════════════════════════════╣");
        log.info("║ 💰 Daily Cost: ${}                                    ║", 
                String.format("%.2f", totalDailyCost));
        log.info("║ 📊 Monthly Projection: ${}                            ║", 
                String.format("%.2f", totalDailyCost * 30));
        log.info("╠═══════════════════════════════════════════════════════════════╣");
        log.info("║ 🎯 Cache Performance:                                         ║");
        log.info("║    {}  ║", cacheStats);
        log.info("╚═══════════════════════════════════════════════════════════════╝");

        // Alert if threshold exceeded
        if (totalDailyCost > dailyCostThreshold) {
            log.error("⚠️⚠️⚠️ COST ALERT ⚠️⚠️⚠️");
            log.error("Daily Google Places API cost (${}) exceeded threshold (${})", 
                     String.format("%.2f", totalDailyCost), 
                     String.format("%.2f", dailyCostThreshold));
            log.error("⚠️⚠️⚠️⚠️⚠️⚠️⚠️⚠️⚠️⚠️⚠️");
            // TODO: Send email/Slack notification to ops team
        }
    }

    /**
     * Log monthly summary on the 1st of each month at 1 AM.
     * TEMPORARILY DISABLED - re-enable after fixing circular dependency
     */
    // @Scheduled(cron = "0 0 1 1 * *") // 1st of month at 1 AM
    public void reportMonthlyMetrics() {
        long textSearchCount = monthlyTextSearchCalls.getAndSet(0);
        long placeDetailsCount = monthlyPlaceDetailsCalls.getAndSet(0);

        double textSearchCost = textSearchCount * COST_PER_TEXT_SEARCH;
        double placeDetailsCost = placeDetailsCount * COST_PER_PLACE_DETAILS;
        double totalMonthlyCost = textSearchCost + placeDetailsCost;

        log.info("╔═══════════════════════════════════════════════════════════════╗");
        log.info("║     MONTHLY GOOGLE PLACES API COST SUMMARY                    ║");
        log.info("╠═══════════════════════════════════════════════════════════════╣");
        log.info("║ 📅 Total API Calls This Month:                                ║");
        log.info("║    • Text Search: {} calls", String.format("%-30d", textSearchCount));
        log.info("║    • Place Details: {} calls", String.format("%-28d", placeDetailsCount));
        log.info("╠═══════════════════════════════════════════════════════════════╣");
        log.info("║ 💰 Total Monthly Cost: ${}                            ║", 
                String.format("%.2f", totalMonthlyCost));
        log.info("║ 📈 Cost Breakdown:                                            ║");
        log.info("║    • Text Search: ${}                                  ║", 
                String.format("%.2f", textSearchCost));
        log.info("║    • Place Details: ${}                                ║", 
                String.format("%.2f", placeDetailsCost));
        log.info("╚═══════════════════════════════════════════════════════════════╝");

        // Alert if monthly threshold exceeded
        if (totalMonthlyCost > monthlyCostThreshold) {
            log.error("⚠️⚠️⚠️ MONTHLY COST ALERT ⚠️⚠️⚠️");
            log.error("Monthly Google Places API cost (${}) exceeded threshold (${})", 
                     String.format("%.2f", totalMonthlyCost), 
                     String.format("%.2f", monthlyCostThreshold));
            log.error("Review usage patterns and consider additional optimizations.");
            log.error("⚠️⚠️⚠️⚠️⚠️⚠️⚠️⚠️⚠️⚠️⚠️⚠️⚠️⚠️");
        }
    }

    /**
     * Get current metrics for monitoring endpoints.
     */
    public String getCurrentMetrics() {
        long dailyTextSearch = dailyTextSearchCalls.get();
        long dailyPlaceDetails = dailyPlaceDetailsCalls.get();
        long monthlyTextSearch = monthlyTextSearchCalls.get();
        long monthlyPlaceDetails = monthlyPlaceDetailsCalls.get();

        double dailyCost = (dailyTextSearch * COST_PER_TEXT_SEARCH) + 
                          (dailyPlaceDetails * COST_PER_PLACE_DETAILS);
        double monthlyCost = (monthlyTextSearch * COST_PER_TEXT_SEARCH) + 
                            (monthlyPlaceDetails * COST_PER_PLACE_DETAILS);

        return String.format(
            "Daily: %d calls ($%.2f) | Monthly: %d calls ($%.2f) | Cache: %s",
            dailyTextSearch + dailyPlaceDetails,
            dailyCost,
            monthlyTextSearch + monthlyPlaceDetails,
            monthlyCost,
            "temporarily disabled" // Circular dependency fix
        );
    }

    /**
     * Manual trigger for testing (can be called via actuator endpoint).
     */
    public void logCurrentStatus() {
        log.info("📊 Current Google Places API Usage:");
        log.info(getCurrentMetrics());
    }
}
