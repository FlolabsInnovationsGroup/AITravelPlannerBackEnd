package com.springboot.FlomadAIplanner.ai.controller;

import com.springboot.FlomadAIplanner.model.entities.Message;
import com.springboot.FlomadAIplanner.model.entities.Session;
import com.springboot.FlomadAIplanner.service.model.MessageService;
import com.springboot.FlomadAIplanner.service.model.SessionService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY;

@Slf4j
@RestController
@RequestMapping("/sessions")
public class AISessionController {

    private final ChatClient chatClient;
    private final MessageService messageService;
    private final SessionService sessionService;
    @Value("classpath:/templates/SessionTitleGenerationPrompt.txt")
    private Resource sessionTitleGenerationPrompt;

    public AISessionController(@Qualifier("openAiChatClient") ChatClient chatClient,
                               MessageService messageService, SessionService sessionService) {
        this.chatClient = chatClient;
        this.messageService = messageService;
        this.sessionService = sessionService;
    }

    @PostMapping("/{id}/title/generate")
    @Operation(summary = "Generate session (chat) title based on the messages from the desired session." +
            "Updates the session title in the DB with the generated title automatically.",
            description = "Generates a session title based on the messages from the desired session." +
                    "Updates the session title in the DB with the generated title automatically.",
            tags = {"sessions", "chat", "title"})
    public ResponseEntity<String> generateTitle(@PathVariable("id") String sessionId) {
        log.info("Generating session title for session with ID: {}", sessionId);
        // getting all messages from the session
        List<Message> messages = messageService.getMessagesBySessionId(sessionId);
        String messagesCombinedText = messages.stream()
                .filter(m -> m.getContent() != null && !m.getContent().isBlank())
                .map(m -> m.getMessageType() + ":\n" + m.getContent())
                .reduce("", (a, b) -> a + "\n------------------\n" + b);
        log.info("Combined messages text: {}", messagesCombinedText);

        log.info("Generating session title...");
        String sessionTitle = chatClient.prompt()
                .system(sessionTitleGenerationPrompt)
                .user(messagesCombinedText)
                .advisors(a -> a.param(CHAT_MEMORY_CONVERSATION_ID_KEY, "session-title"))
                .call()
                .content();
        log.info("Generated session title: {}", sessionTitle);

        // updating the session title
        Session session = sessionService.findById(sessionId);
        session.setTitle(sessionTitle);
        sessionService.save(session);
        log.info("Session title updated in the DB.");

        return ResponseEntity.ok(sessionTitle);
    }

}
