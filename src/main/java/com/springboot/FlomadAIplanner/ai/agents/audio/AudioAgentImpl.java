package com.springboot.FlomadAIplanner.ai.agents.audio;

import com.springboot.FlomadAIplanner.ai.agents.Agent;
import com.springboot.FlomadAIplanner.ai.agents.text.TextAgent;
import com.springboot.FlomadAIplanner.ai.dto.AIMessageDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.util.Objects;

@Slf4j
@Component
public class AudioAgentImpl implements Agent {

    private final TextAgent<String, Flux<String>> textAgent;
    private final AudioTranscriptionService audioTranscriptionService;

    public AudioAgentImpl(@Qualifier("openAiAgent") TextAgent<String, Flux<String>> textAgent,
                          AudioTranscriptionService audioTranscriptionService) {
        this.textAgent = textAgent;
        this.audioTranscriptionService = audioTranscriptionService;
    }

    @Override
    public boolean supports(AIMessageDTO inputMessage) {
        if (inputMessage.getMedia() == null) {
            log.error("No media attached in the AIMessageDTO.");
            return false;
        }
        String contentType = Objects.requireNonNull(inputMessage.getMedia().getContentType());
        return contentType.startsWith("audio/");
    }

    @Override
    public String processCallMessage(AIMessageDTO aiMessage) {
        try {
            if (aiMessage.getMedia().isEmpty()) {
                throw new IllegalArgumentException("Audio file is empty.");
            }
            String transcribedText = audioTranscriptionService.transcribeAudio(aiMessage.getMedia());
            aiMessage.setMessage(transcribedText);
            return textAgent.processCallMessage(aiMessage);
        } catch (IllegalArgumentException | IOException e) {
            log.error("Error processing audio call message: {}", aiMessage.getMedia().getOriginalFilename(), e);
            throw new RuntimeException("Error processing audio message: " + e.getMessage(), e);
        }
    }

    @Override
    public Flux<String> processStreamMessage(AIMessageDTO aiMessage) {
        try {
            if (aiMessage.getMedia().isEmpty()) {
                throw new IllegalArgumentException("Audio stream is empty.");
            }
            String transcribedText = audioTranscriptionService.transcribeAudio(aiMessage.getMedia());
            aiMessage.setMessage(transcribedText);
            return textAgent.processStreamMessage(aiMessage);
        } catch (IllegalArgumentException | IOException e) {
            log.error("Error processing audio stream: {}", aiMessage.getMedia().getOriginalFilename(), e);
            return Flux.error(new RuntimeException("Error processing audio stream: " + e.getMessage(), e));
        }
    }

    @Override
    public <T> T processEntityMessage(AIMessageDTO aiMessage, Class<T> returnClassType) {
        log.warn("Entity message processing is not supported for AudioAgent.");
        throw new UnsupportedOperationException("Entity message processing is not supported for AudioAgent.");
    }
}
