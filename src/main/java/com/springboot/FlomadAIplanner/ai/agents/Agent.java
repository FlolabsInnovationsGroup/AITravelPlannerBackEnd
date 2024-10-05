package com.springboot.FlomadAIplanner.ai.agents;

import com.springboot.FlomadAIplanner.ai.dto.AIMessageDTO;

public interface Agent {
    boolean supports(AIMessageDTO inputMessage);
}
