package com.springboot.FlomadAIplanner.ai.analytics;

import com.springboot.FlomadAIplanner.ai.analytics.consts.MetricsConstants;
import com.springboot.FlomadAIplanner.ai.analytics.util.MetricsUtils;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Aspect for tracking metrics for 'streaming' chat models
 * <p> Serves for the analytical purposes only.
 * <p> This aspect tracks the stream method of streaming chat models and records metrics
 * for the input tokens, output tokens, and duration of the call.
 */
@Slf4j
@Aspect
@Component
public class StreamingChatModelMetricsAspect extends BaseChatModelMetricsAspect {

    @Autowired
    public StreamingChatModelMetricsAspect(MeterRegistry meterRegistry) {
        super(meterRegistry);
    }

    /**
     * Pointcut for the stream method of streaming chat models
     * <p> This pointcut captures the stream method of streaming chat models
     * and passes the prompt object as an argument.
     *
     * @param prompt the prompt object
     */
    @Pointcut("execution(* org.springframework.ai.chat.model.StreamingChatModel.stream(org.springframework.ai.chat.prompt.Prompt)) && args(prompt)")
    public void streamMethod(Prompt prompt) {
    }

    /**
     * Around advice for the stream method of streaming chat models
     * <p> This advice allows to perform additional logic processing before and after the method we captured as a pointcut.
     * Also tracks the stream method of streaming chat models and records metrics
     * for the input tokens, output tokens, and duration of the call.
     *
     * @param pjp    the proceeding join point
     * @param prompt the prompt object
     * @return the chat response flux
     * @throws Throwable if an error occurs
     */
    @Around(value = "streamMethod(prompt)", argNames = "pjp,prompt")
    public Object trackStreamMethod(ProceedingJoinPoint pjp, Prompt prompt) throws Throwable {
        String modelName = pjp.getTarget().getClass().getSimpleName();
        log.info("Tracking stream method for model: {}", modelName);

        long startTime = System.currentTimeMillis();
        long inputTokens = MetricsUtils.countTokens(prompt.getContents());
        AtomicLong totalOutputTokens = new AtomicLong(0);

        Flux<ChatResponse> responseFlux = (Flux<ChatResponse>) pjp.proceed();

        Flux<ChatResponse> monitoredFlux = responseFlux
                .doOnNext(response -> {
                    String outputContent = (response.getResult() != null && response.getResult().getOutput() != null)
                            ? response.getResult().getOutput().getContent()
                            : "";
                    totalOutputTokens.addAndGet(MetricsUtils.countTokens(outputContent));
                })
                .doOnComplete(() -> {
                    long duration = System.currentTimeMillis() - startTime;
                    log.debug("Metrics calculated for model: {}, totalInputTokens: {}, totalOutputTokens: {}, duration: {}", modelName, inputTokens, totalOutputTokens.get(), duration);

                    // Record metrics after the stream is complete
                    String[] tags = {MetricsConstants.MODEL_TAG, modelName, MetricsConstants.METHOD_TAG, MetricsConstants.STREAM_TAG};
                    MetricsUtils.recordChatModelMetrics(meterRegistry, inputTokens, totalOutputTokens.get(), duration, tags);
                    log.debug("Metrics recorded for model: {}", modelName);
                });

        // Subscribe to the flux to ensure side effect methods are executed
        monitoredFlux.subscribe();

        return monitoredFlux;
    }
}
