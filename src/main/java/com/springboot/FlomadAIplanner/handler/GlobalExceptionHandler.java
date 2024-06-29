package com.springboot.FlomadAIplanner.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.PromptChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;

import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_RETRIEVE_SIZE_KEY;

@Slf4j
@ControllerAdvice
@RestController
public class GlobalExceptionHandler {

    private final ChatClient chatClient;

    public GlobalExceptionHandler(ChatClient.Builder builder, ChatMemory chatMemory,
                                  @Value("classpath:/templates/ExceptionHandlingPrompt.txt") Resource promptResource) {
        this.chatClient = builder
                .defaultSystem(promptResource)
                .defaultAdvisors(new PromptChatMemoryAdvisor(chatMemory))
                .build();
    }

    @ExceptionHandler(Exception.class)
    @ResponseBody
    public ResponseEntity<String> handleException(Exception e) {
        log.error("Exception caught: ", e);

        // Send stack trace to LLM
        ExceptionRecord response = chatClient.prompt()
                .user("Here is the stack trace of an exception:\n" + Arrays.toString(e.getStackTrace()))
                .advisors(a -> a.param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                .call()
                .entity(ExceptionRecord.class);

        log.info("RootCause explanation from LLM: {}", response.technicalRuteCauseExceptionExplanationForDevelopers());

        // Return the response
        return new ResponseEntity<>(response.customerErrorMessage(), response.httpStatus());
    }
}
