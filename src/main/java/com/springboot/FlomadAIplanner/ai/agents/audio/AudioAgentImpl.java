package com.springboot.FlomadAIplanner.ai.agents.audio;

import com.springboot.FlomadAIplanner.ai.agents.Agent;
import com.springboot.FlomadAIplanner.ai.agents.text.TextAgent;
import com.springboot.FlomadAIplanner.ai.dto.AIMessageDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
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
        String contentType = Objects.requireNonNull(inputMessage.getMedia().getContentType(), "Content type is null");
        return contentType.startsWith("audio/");
    }

    @Override
    public String processCallMessage(AIMessageDTO aiMessage) {
        try {
            MultipartFile mediaFile = aiMessage.getMedia();
            if (mediaFile == null || mediaFile.isEmpty()) {
                throw new IllegalArgumentException("Audio file is empty or null.");
            }
            String transcribedText = audioTranscriptionService.transcribeAudio(mediaFile);
            aiMessage.setMessage(transcribedText);
            return textAgent.processCallMessage(aiMessage);
        } catch (IllegalArgumentException | IOException e) {
            log.error("Error processing audio call message: {}",
                    aiMessage.getMedia() != null ? aiMessage.getMedia().getOriginalFilename() : "Unknown file", e);
            throw new RuntimeException("Error processing audio message: " + e.getMessage(), e);
        }
    }

    @Override
    public Flux<String> processStreamMessage(AIMessageDTO aiMessage) {
        try {
            MultipartFile mediaFile = aiMessage.getMedia();
            if (mediaFile == null || mediaFile.isEmpty()) {
                throw new IllegalArgumentException("Audio stream is empty or null.");
            }
            String transcribedText = audioTranscriptionService.transcribeAudio(mediaFile);
            aiMessage.setMessage(transcribedText);
            return textAgent.processStreamMessage(aiMessage);
        } catch (IllegalArgumentException | IOException e) {
            log.error("Error processing audio stream: {}",
                    aiMessage.getMedia() != null ? aiMessage.getMedia().getOriginalFilename() : "Unknown file", e);
            return Flux.error(new RuntimeException("Error processing audio stream: " + e.getMessage(), e));
        }
    }

    @Override
    public <T> T processEntityMessage(AIMessageDTO aiMessage, Class<T> returnClassType) {
        log.warn("Entity message processing is not supported for AudioAgent.");
        throw new UnsupportedOperationException("Entity message processing is not supported for AudioAgent.");
    }
}