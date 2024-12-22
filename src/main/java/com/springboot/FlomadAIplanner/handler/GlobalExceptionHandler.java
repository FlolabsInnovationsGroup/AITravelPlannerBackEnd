package com.springboot.FlomadAIplanner.handler;

import com.springboot.FlomadAIplanner.exceptions.validators.GuestMessageLimitReachedException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.PromptChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;

import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY;
import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_RETRIEVE_SIZE_KEY;

@Slf4j
@ControllerAdvice
@RestController
public class GlobalExceptionHandler {

    private final ChatClient chatClient;

    public GlobalExceptionHandler(ChatClient.Builder builder, @Qualifier("exceptionHandlingMemory") ChatMemory chatMemory,
                                  @Value("classpath:/templates/ExceptionHandlingPrompt.txt") Resource promptResource) {
        this.chatClient = builder
                .defaultSystem(promptResource)
                .defaultAdvisors(new PromptChatMemoryAdvisor(chatMemory))
                .build();
    }

    @ExceptionHandler(GuestMessageLimitReachedException.class)
    @ResponseBody
    public ResponseEntity<String> handleGuestMessageLimitReached(GuestMessageLimitReachedException ex) {
        log.error("Guest message limit reached: ", ex);

        // Custom message or interaction with LLM, if needed, for this specific exception
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(Exception.class)
    @ResponseBody
    public ResponseEntity<String> handleException(HttpServletRequest request, Exception e) {
        log.error("Exception caught: ", e);

        String sessionId = request.getSession().getId();

        // Send stack trace to LLM
        ExceptionRecord response = chatClient.prompt()
                .user("Here is the stack trace of an exception:\n" + Arrays.toString(e.getStackTrace()))
                .advisors(a -> a
                        .param(CHAT_MEMORY_CONVERSATION_ID_KEY, sessionId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                .call()
                .entity(ExceptionRecord.class);

        log.info("RootCause explanation from LLM: {}", response.technicalRuteCauseExceptionExplanationForDevelopers());

        // Return the response
        return new ResponseEntity<>(response.customerErrorMessage(), response.httpStatus());
    }

}
