package com.springboot.FlomadAIplanner.ai.functions;

import java.util.function.Function;

/**
 * AIFunction interface is a functional interface for AI functions.
 * It extends the Function interface.
 * <p> It provides a method to apply the function to the given input.
 * <p> It used to define AI functions that can be called by the LLM.
 *
 * @param <Request>  the type of the input to the function
 * @param <Response> the type of the result of the function
 */
public interface AIFunction<Request, Response> extends Function<Request, Response> {

    /**
     * Applies this function to the given input.
     *
     * @param request the input to the function
     * @return the result of the function
     */
    Response apply(Request request);

}
