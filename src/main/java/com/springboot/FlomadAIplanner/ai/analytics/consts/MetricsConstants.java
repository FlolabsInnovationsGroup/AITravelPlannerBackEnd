package com.springboot.FlomadAIplanner.ai.analytics.consts;

/**
 * MetricsConstants class is a class that contains constants for metrics.
 * It provides constants for metric names and tags.
 */
public class MetricsConstants {

    // Chat model metrics (for every call to the LLM)
    public static final String CHAT_MODEL_EXECUTE_TIMER = "chat_model_execute";
    public static final String CHAT_MODEL_INPUT_TOKENS = "chat_model_input_tokens";
    public static final String CHAT_MODEL_OUTPUT_TOKENS = "chat_model_output_tokens";

    // Function metrics
    public static final String FUNCTION_EXECUTE_TIMER = "function_execute";
    public static final String FUNCTION_INPUT_DESCRIPTION_LENGTH = "function_input_description_length";
    public static final String FUNCTION_INPUT_TOKENS = "function_input_tokens";
    public static final String FUNCTION_OUTPUT_TOKENS = "function_output_tokens";


    // Tags
    public static final String MODEL_TAG = "model";
    public static final String FUNCTION_TAG = "function";
    public static final String METHOD_TAG = "method";
    public static final String CALL_TAG = "call";
    public static final String STREAM_TAG = "stream";
}

