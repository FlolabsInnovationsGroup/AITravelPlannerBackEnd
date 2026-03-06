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
 * Performance Monitoring Service.
 *
 * <p>Monitoring and metrics for AI Travel Planner.
 * Injected for manual performance tracking.
 *
 * <p>Features: AI function timing, counter tracking,
 * threshold alerts, error rate monitoring.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PerformanceMonitoringService {

    /** Threshold in ms above which execution is considered slow. */
    private static final long SLOW_THRESHOLD_MS = 5000;
    /** Threshold in ms above which execution is logged at info. */
    private static final long INFO_THRESHOLD_MS = 1000;
    /** Threshold in ms for "lightning" tier. */
    private static final long LIGHTNING_MS = 100;
    /** Threshold in ms for "fast" tier. */
    private static final long FAST_MS = 1000;
    /** Threshold in ms for "standard" tier. */
    private static final long STANDARD_MS = 5000;

    /** Meter registry. */
    private final MeterRegistry meterRegistry;

    /**
     * Tracks AI function execution performance.
     *
     * @param <T> return type
     * @param functionName function name
     * @param className class name
     * @param operation operation to execute
     * @return result of operation
     */
    public <T> T trackAIFunctionExecution(
            final String functionName,
            final String className,
            final java.util.function.Supplier<T> operation) {

        Instant start = Instant.now();

        Timer functionTimer = Timer.builder("ai_function.execution_time")
                .description("Execution time AI function: " + functionName)
                .tag("function", functionName)
                .tag("class", className)
                .register(meterRegistry);

        Counter functionCounter = Counter.builder("ai_function.calls")
                .description("Number of calls to AI function: " + functionName)
                .tag("function", functionName)
                .tag("class", className)
                .register(meterRegistry);

        try {
            log.debug("AI function start: {}.{}", className, functionName);

            T result = operation.get();

            Duration executionTime = Duration.between(start, Instant.now());

            functionTimer.record(executionTime);
            functionCounter.increment();

            long executionMs = executionTime.toMillis();
            if (executionMs > SLOW_THRESHOLD_MS) {
                log.warn("Slow AI function execution: {}.{} took {}ms",
                        className, functionName, executionMs);
            } else if (executionMs > INFO_THRESHOLD_MS) {
                log.info("AI function execution: {}.{} took {}ms",
                        className, functionName, executionMs);
            } else {
                log.debug("AI function execution: {}.{} took {}ms",
                        className, functionName, executionMs);
            }

            return result;

        } catch (Exception e) {
            Duration executionTime = Duration.between(start, Instant.now());

            Counter errorCounter = Counter.builder("ai_function.errors")
                    .description("Errors in AI function: " + functionName)
                    .tag("function", functionName)
                    .tag("class", className)
                    .tag("error", e.getClass().getSimpleName())
                    .register(meterRegistry);

            errorCounter.increment();

            log.error("AI function failed: {}.{} after {}ms: {}",
                    className, functionName,
                    executionTime.toMillis(), e.getMessage(), e);

            throw new RuntimeException("AI function execution failed", e);
        }
    }

    /**
     * Tracks chat service operations.
     *
     * @param <T> return type
     * @param methodName method name
     * @param serviceName service name
     * @param operation operation to execute
     * @return result of operation
     */
    public <T> T trackChatServiceExecution(
            final String methodName,
            final String serviceName,
            final java.util.function.Supplier<T> operation) {
        Instant start = Instant.now();

        Timer serviceTimer = Timer.builder("chat_service.execution_time")
                .description("Execution time chat method: " + methodName)
                .tag("service", serviceName)
                .tag("method", methodName)
                .register(meterRegistry);

        Counter serviceCounter = Counter.builder("chat_service.calls")
                .description("Calls to chat service method: " + methodName)
                .tag("service", serviceName)
                .tag("method", methodName)
                .register(meterRegistry);

        try {
            T result = operation.get();

            Duration executionTime = Duration.between(start, Instant.now());
            serviceTimer.record(executionTime);
            serviceCounter.increment();

            long executionMs = executionTime.toMillis();

            String performanceTier = determinePerformanceTier(executionMs);
            Counter tierCounter =
                    Counter.builder("chat_service.performance_tier")
                    .description("Performance tier")
                    .tag("tier", performanceTier)
                    .tag("service", serviceName)
                    .register(meterRegistry);
            tierCounter.increment();

            log.debug("Chat execution: {}.{} took {}ms ({})",
                    serviceName, methodName, executionMs, performanceTier);

            return result;

        } catch (Exception e) {
            Counter errorCounter = Counter.builder("chat_service.errors")
                    .description("Errors in chat service: " + methodName)
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
     * Tracks WebSocket operations.
     *
     * @param operationName operation name
     * @param operation operation to run
     */
    public void trackWebSocketOperation(
            final String operationName,
            final Runnable operation) {
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

            log.error("WebSocket operation failed: {} - Error: {}",
                    operationName, e.getMessage(), e);
            throw new RuntimeException("WebSocket operation failed", e);
        }
    }

    /**
     * Manually increments a counter for any operation.
     *
     * @param name counter name
     * @param description description
     * @param tags key-value tags (pairs)
     */
    public void incrementCounter(
            final String name,
            final String description,
            final String... tags) {
        Counter.Builder builder = Counter.builder(name)
                .description(description);

        for (int i = 0; i < tags.length; i += 2) {
            if (i + 1 < tags.length) {
                builder.tag(tags[i], tags[i + 1]);
            }
        }

        Counter counter = builder.register(meterRegistry);
        counter.increment();
    }

    /**
     * Records a timer measurement.
     *
     * @param name timer name
     * @param description description
     * @param duration duration
     * @param tags key-value tags (pairs)
     */
    public void recordTimer(
            final String name,
            final String description,
            final Duration duration,
            final String... tags) {
        Timer.Builder builder = Timer.builder(name)
                .description(description);

        for (int i = 0; i < tags.length; i += 2) {
            if (i + 1 < tags.length) {
                builder.tag(tags[i], tags[i + 1]);
            }
        }

        Timer timer = builder.register(meterRegistry);
        timer.record(duration);
    }

    /**
     * Determines performance tier based on execution time in ms.
     *
     * @param executionMs execution time in milliseconds
     * @return tier (lightning, fast, standard, slow)
     */
    private String determinePerformanceTier(final long executionMs) {
        if (executionMs <= LIGHTNING_MS) {
            return "lightning";
        } else if (executionMs <= FAST_MS) {
            return "fast";
        } else if (executionMs <= STANDARD_MS) {
            return "standard";
        } else {
            return "slow";
        }
    }
}

