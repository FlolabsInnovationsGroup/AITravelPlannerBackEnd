package com.springboot.FlomadAIplanner.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * ChatClientSuggestedProperties represents suggested properties for a chat client.
 * It includes multiple function names and a system prompt description.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Slf4j
@Component
public class ChatClientSuggestedProperties {
    private List<String> functionNames = Collections.emptyList();  // could be several
    private String systemPrompt; // should be only one

}
