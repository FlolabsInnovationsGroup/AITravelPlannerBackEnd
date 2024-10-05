package com.springboot.FlomadAIplanner.ai.agents.text;

import com.springboot.FlomadAIplanner.ai.agents.Agent;
import com.springboot.FlomadAIplanner.ai.agents.CallMessageProcessing;
import com.springboot.FlomadAIplanner.ai.agents.EntityReturnMessageProcessing;
import com.springboot.FlomadAIplanner.ai.agents.StreamMessageProcessing;
import reactor.core.publisher.Flux;

/**
 * Interface that combines call, entity, and stream message processing capabilities.
 */
public interface TextAgent<C, S extends Flux<?>> extends Agent, CallMessageProcessing<C>, EntityReturnMessageProcessing, StreamMessageProcessing<S> {
    // This interface inherits all methods from the extended interfaces

}