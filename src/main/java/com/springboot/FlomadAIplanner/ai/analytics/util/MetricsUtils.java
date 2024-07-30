package com.springboot.FlomadAIplanner.ai.analytics.util;

import com.springboot.FlomadAIplanner.ai.analytics.consts.MetricsConstants;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

/**
 * Utility class for recording metrics
 */
@Slf4j
public class MetricsUtils {

    public static long countTokens(String content) {
        log.debug("Counting tokens for content");
        if (content == null || content.isEmpty()) {
            return 0;
        }
        return content.length() / 4;  // Estimate tokens as one token per four characters
    }

    public static void recordChatModelMetrics(MeterRegistry meterRegistry, long inputTokens, long outputTokens, long duration, String... tags) {
        log.debug("Recording chat model metrics");
        Timer methodTimer = meterRegistry.timer(MetricsConstants.CHAT_MODEL_EXECUTE_TIMER, tags);
        methodTimer.record(duration, TimeUnit.MILLISECONDS);

        DistributionSummary inputTokensSummary = meterRegistry.summary(MetricsConstants.CHAT_MODEL_INPUT_TOKENS, tags);
        DistributionSummary outputTokensSummary = meterRegistry.summary(MetricsConstants.CHAT_MODEL_OUTPUT_TOKENS, tags);

        inputTokensSummary.record(inputTokens);
        outputTokensSummary.record(outputTokens);
    }

    public static void recordFunctionMetrics(MeterRegistry meterRegistry, int inputDescriptionLength, long inputTokens, long outputTokens, long duration, String... tags) {
        log.debug("Recording function metrics");
        Timer methodTimer = meterRegistry.timer(MetricsConstants.FUNCTION_EXECUTE_TIMER, tags);
        methodTimer.record(duration, TimeUnit.MILLISECONDS);

        DistributionSummary inputDescriptionSummary = meterRegistry.summary(MetricsConstants.FUNCTION_INPUT_DESCRIPTION_LENGTH, tags);
        DistributionSummary inputTokensSummary = meterRegistry.summary(MetricsConstants.FUNCTION_INPUT_TOKENS, tags);
        DistributionSummary outputTokensSummary = meterRegistry.summary(MetricsConstants.FUNCTION_OUTPUT_TOKENS, tags);

        inputDescriptionSummary.record(inputDescriptionLength);
        inputTokensSummary.record(inputTokens);
        outputTokensSummary.record(outputTokens);
        log.debug("Recorded function metrics");
    }
}
