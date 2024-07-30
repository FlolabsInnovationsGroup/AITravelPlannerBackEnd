package com.springboot.FlomadAIplanner.ai.analytics;

import io.micrometer.core.instrument.MeterRegistry;

/**
 * Base class for tracking metrics for chat models
 * <p> Serves for the analytical purposes only.
 * <p> This class provides the base functionality for tracking metrics for chat models.
 * <p>
 * <p> Now is empty, but can be extended in the future.
 */
public abstract class BaseChatModelMetricsAspect {

    protected final MeterRegistry meterRegistry;

    protected BaseChatModelMetricsAspect(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

}
