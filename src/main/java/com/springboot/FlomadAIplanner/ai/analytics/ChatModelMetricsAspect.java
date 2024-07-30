package com.springboot.FlomadAIplanner.ai.analytics;

import com.springboot.FlomadAIplanner.ai.analytics.consts.MetricsConstants;
import com.springboot.FlomadAIplanner.ai.analytics.util.MetricsUtils;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Aspect for tracking metrics for 'calling' chat models
 * <p> Serves for the analytical purposes only.
 * <p> This aspect tracks the call method of chat models and records metrics
 * for the input tokens, output tokens, and duration of the call.
 */
@Slf4j
@Aspect
@Component
public class ChatModelMetricsAspect extends BaseChatModelMetricsAspect {

    @Autowired
    public ChatModelMetricsAspect(MeterRegistry meterRegistry) {
        super(meterRegistry);
    }

    /**
     * Pointcut for the call method of chat models
     * <p> This pointcut captures the call method of chat models
     * and passes the prompt object as an argument.
     *
     * @param prompt the prompt object
     */
    @Pointcut("execution(* org.springframework.ai.chat.model.ChatModel.call(org.springframework.ai.chat.prompt.Prompt)) && args(prompt)")
    public void callMethod(Prompt prompt) {
    }

    /**
     * Around advice for the call method of chat models
     * <p> This advice allows to perform additional logic processing before and after the method we captured as a pointcut.
     * Also tracks the call method of chat models and records metrics
     * for the input tokens, output tokens, and duration of the call.
     *
     * @param pjp    the proceeding join point
     * @param prompt the prompt object
     * @return the chat response object
     * @throws Throwable if an error occurs
     */
    @Around(value = "callMethod(prompt)", argNames = "pjp,prompt")
    @SuppressWarnings("unused")
    public Object trackCallMethod(ProceedingJoinPoint pjp, Prompt prompt) throws Throwable {
        String modelName = pjp.getTarget().getClass().getSimpleName();
        log.info("Tracking call method for model: {}", modelName);

        long startTime = System.currentTimeMillis();
        ChatResponse result = (ChatResponse) pjp.proceed();
        long endTime = System.currentTimeMillis();

        // Calculate metrics
        Usage usage = result.getMetadata().getUsage();
        long duration = endTime - startTime;
        long inputTokens = usage.getPromptTokens();
        long outputTokens = usage.getGenerationTokens();

        log.debug("Metrics calculated for model: {}, inputTokens: {}, outputTokens: {}, duration: {}", modelName, inputTokens, outputTokens, duration);
        // Record metrics
        String[] tags = {MetricsConstants.MODEL_TAG, modelName, MetricsConstants.METHOD_TAG, MetricsConstants.CALL_TAG};
        MetricsUtils.recordChatModelMetrics(meterRegistry, inputTokens, outputTokens, duration, tags);
        log.debug("Metrics recorded for model: {}", modelName);

//        System.err.println(" Input: " + prompt.getContents() + "\n" +
//                " Output: " + output + "\n" +
//                " Duration: " + duration + "ms");


        System.err.println("USAGE Input Tokens: " + result.getMetadata().getUsage().getPromptTokens() + "\n" +
                "USAGE Output Tokens: " + result.getMetadata().getUsage().getGenerationTokens() + "\n" +
                "USAGE general tokens: " + result.getMetadata().getUsage().getTotalTokens());

        return result;
    }
}
