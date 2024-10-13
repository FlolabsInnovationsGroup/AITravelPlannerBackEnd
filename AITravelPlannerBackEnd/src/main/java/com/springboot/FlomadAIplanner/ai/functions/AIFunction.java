package com.springboot.FlomadAIplanner.ai.functions;

import java.util.function.Function;

public interface AIFunction<Request, Response> extends Function<Request, Response> {

    Response apply(Request request);

}
