package com.springboot.FlomadAIplanner.monitoring;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

/**
 * Performance Monitoring Service
 * 
 * Provides comprehensive monitoring and metrics tracking for AI Travel Planner operations.
 * This service can be injected into other services for manual performance tracking.
 * 
 * Features:
 * - Manual timing of AI function executions
 * - Counter tracking for function calls
 * - Performance threshold alerts
 * - Error rate monitoring
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PerformanceMonitoringService {

    private final MeterRegistry meterRegistry;

    /**
     * Track AI function execution performance
     */
    public <T> T trackAIFunctionExecution(String functionName, String className, 
                                         java.util.function.Supplier<T> operation) {
        
        Instant start = Instant.now();
        
        // Create meters for this specific function
        Timer functionTimer = Timer.builder("ai_function.execution_time")
                .description("Execution time for AI function: " + functionName)
                .tag("function", functionName)
                .tag("class", className)
                .register(meterRegistry);
                
        Counter functionCounter = Counter.builder("ai_function.calls")
                .description("Number of calls to AI function: " + functionName)
                .tag("function", functionName)
                .tag("class", className)
                .register(meterRegistry);

        try {
            log.debug("Starting execution of AI function: {}.{}", className, functionName);
            
            T result = operation.get();
            
            Duration executionTime = Duration.between(start, Instant.now());
            
            // Record successful execution
            functionTimer.record(executionTime);
            functionCounter.increment();
            
            // Log performance warnings for slow functions
            long executionMs = executionTime.toMillis();
            if (executionMs > 5000) {
                log.warn("Slow AI function execution: {}.{} took {}ms", className, functionName, executionMs);
            } else if (executionMs > 1000) {
                log.info("AI function execution: {}.{} took {}ms", className, functionName, executionMs);
            } else {
                log.debug("AI function execution: {}.{} took {}ms", className, functionName, executionMs);
            }
            
            return result;
            
        } catch (Exception e) {
            Duration executionTime = Duration.between(start, Instant.now());
            
            // Record failed execution
            Counter errorCounter = Counter.builder("ai_function.errors")
                    .description("Number of errors in AI function: " + functionName)
                    .tag("function", functionName)
                    .tag("class", className)
                    .tag("error", e.getClass().getSimpleName())
                    .register(meterRegistry);
            
            errorCounter.increment();
            
            log.error("AI function execution failed: {}.{} after {}ms - Error: {}", 
                     className, functionName, executionTime.toMillis(), e.getMessage(), e);
            
            throw new RuntimeException("AI function execution failed", e);
        }
    }

    /**
     * Track chat service operations
     */
    public <T> T trackChatServiceExecution(String methodName, String serviceName, 
                                          java.util.function.Supplier<T> operation) {
        Instant start = Instant.now();
        
        Timer serviceTimer = Timer.builder("chat_service.execution_time")
                .description("Execution time for chat service method: " + methodName)
                .tag("service", serviceName)
                .tag("method", methodName)
                .register(meterRegistry);
                
        Counter serviceCounter = Counter.builder("chat_service.calls")
                .description("Number of calls to chat service method: " + methodName)
                .tag("service", serviceName)
                .tag("method", methodName)
                .register(meterRegistry);

        try {
            T result = operation.get();
            
            Duration executionTime = Duration.between(start, Instant.now());
            serviceTimer.record(executionTime);
            serviceCounter.increment();
            
            long executionMs = executionTime.toMillis();
            
            // Track performance tiers
            String performanceTier = determinePerformanceTier(executionMs);
            Counter tierCounter = Counter.builder("chat_service.performance_tier")
                    .description("Performance tier classification")
                    .tag("tier", performanceTier)
                    .tag("service", serviceName)
                    .register(meterRegistry);
            tierCounter.increment();
            
            log.debug("Chat service execution: {}.{} took {}ms ({})", 
                     serviceName, methodName, executionMs, performanceTier);
            
            return result;
            
        } catch (Exception e) {
            Counter errorCounter = Counter.builder("chat_service.errors")
                    .description("Number of errors in chat service: " + methodName)
                    .tag("service", serviceName)
                    .tag("method", methodName)
                    .tag("error", e.getClass().getSimpleName())
                    .register(meterRegistry);
            
            errorCounter.increment();
            
            log.error("Chat service execution failed: {}.{} - Error: {}", 
                     serviceName, methodName, e.getMessage(), e);
            
            throw new RuntimeException("Chat service execution failed", e);
        }
    }

    /**
     * Track WebSocket operations
     */
    public void trackWebSocketOperation(String operationName, Runnable operation) {
        Counter wsCounter = Counter.builder("websocket.operations")
                .description("WebSocket operations count")
                .tag("operation", operationName)
                .register(meterRegistry);

        try {
            operation.run();
            wsCounter.increment();
            
            log.debug("WebSocket operation completed: {}", operationName);
            
        } catch (Exception e) {
            Counter errorCounter = Counter.builder("websocket.errors")
                    .description("WebSocket operation errors")
                    .tag("operation", operationName)
                    .tag("error", e.getClass().getSimpleName())
                    .register(meterRegistry);
            
            errorCounter.increment();
            
            log.error("WebSocket operation failed: {} - Error: {}", operationName, e.getMessage(), e);
            throw new RuntimeException("WebSocket operation failed", e);
        }
    }

    /**
     * Manually increment a counter for any operation
     */
    public void incrementCounter(String name, String description, String... tags) {
        Counter.Builder builder = Counter.builder(name).description(description);
        
        for (int i = 0; i < tags.length; i += 2) {
            if (i + 1 < tags.length) {
                builder.tag(tags[i], tags[i + 1]);
            }
        }
        
        Counter counter = builder.register(meterRegistry);
        counter.increment();
    }

    /**
     * Record a timer measurement
     */
    public void recordTimer(String name, String description, Duration duration, String... tags) {
        Timer.Builder builder = Timer.builder(name).description(description);
        
        for (int i = 0; i < tags.length; i += 2) {
            if (i + 1 < tags.length) {
                builder.tag(tags[i], tags[i + 1]);
            }
        }
        
        Timer timer = builder.register(meterRegistry);
        timer.record(duration);
    }

    /**
     * Determine performance tier based on execution time
     */
    private String determinePerformanceTier(long executionMs) {
        if (executionMs <= 100) {
            return "lightning"; // Ultra-fast responses
        } else if (executionMs <= 1000) {
            return "fast"; // Quick responses
        } else if (executionMs <= 5000) {
            return "standard"; // Normal responses
        } else {
            return "slow"; // Needs optimization
        }
    }
}