package com.springboot.FlomadAIplanner.ai.agents.audio;

import com.springboot.FlomadAIplanner.ai.agents.Agent;
import com.springboot.FlomadAIplanner.ai.agents.messageProcessing.CallMessageProcessingStrategy;
import com.springboot.FlomadAIplanner.ai.dto.AIMessageDTO;
import com.springboot.FlomadAIplanner.ai.dto.MessageProcessingStrategyDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;

import java.io.IOException;

@Slf4j
@Component
public class AudioAgentImpl implements Agent {
    private final CallMessageProcessingStrategy callMessageProcessingStrategy;

    @Autowired
    public AudioAgentImpl(CallMessageProcessingStrategy callMessageProcessingStrategy) {
        this.callMessageProcessingStrategy = callMessageProcessingStrategy;
    }

    @Override
    public boolean supports(AIMessageDTO inputMessage) {
        if (inputMessage.getMedia() == null) {
            log.error("No media attached in the AIMessageDTO.");
            return false;
        }

        if (!inputMessage.getMedia().getContentType().startsWith("audio/")) {
            log.error("Unsupported media type: {}", inputMessage.getMedia().getContentType());
            return false;
        }

        return true;
    }

    @Override
    public String processCallMessage(AIMessageDTO aiMessage) {
        log.debug("Processing audio call message: {}", aiMessage.getMedia().getOriginalFilename());

        try {
            // Validate the media file
            if (aiMessage.getMedia().isEmpty()) {
                throw new IllegalArgumentException("Audio file is empty.");
            }

            // Transcribe the audio file
            String transcribedText = transcribeAudio(aiMessage.getMedia());

            // Create a MessageProcessingStrategyDTO object for processing
            MessageProcessingStrategyDTO messageProcessingStrategyDTO = MessageProcessingStrategyDTO
                    .builder()
                    .aiMessage(aiMessage)
                    .systemPrompt(new Prompt(transcribedText))  // Use transcribed text as the system prompt
                    .build();

            // Process the transcribed text using the call message strategy
            return callMessageProcessingStrategy.processMessage(messageProcessingStrategyDTO);

        } catch (IllegalArgumentException | IOException e) {
            log.error("Error processing audio call message: {}", aiMessage.getMedia().getOriginalFilename(), e);
            throw new RuntimeException("Error processing audio message: " + e.getMessage(), e);
        }
    }

    @Override
    public Flux<String> processStreamMessage(AIMessageDTO aiMessage) {
        log.debug("Processing audio stream message: {}", aiMessage.getMedia().getOriginalFilename());
        try {
            if (aiMessage.getMedia().isEmpty()) {
                throw new IllegalArgumentException("Audio stream is empty.");
            }

            // Transcribe and stream the audio
            String transcribedText = transcribeAudio(aiMessage.getMedia());
            return Flux.just(transcribedText); // Placeholder for actual streaming logic
        } catch (IllegalArgumentException | IOException e) {
            log.error("Error processing audio stream: {}", aiMessage.getMedia().getOriginalFilename(), e);
            return Flux.error(new RuntimeException("Error processing audio stream: " + e.getMessage(), e));
        }
    }

    /**
     * Transcribes the given audio file to text.
     * Replace this with actual transcription logic.
     *
     * @param audioFile the audio file to transcribe
     * @return the transcribed text
     * @throws IOException if an I/O error occurs during file processing
     */
    private String transcribeAudio(MultipartFile audioFile) throws IOException {
        log.info("Transcribing audio file: {}", audioFile.getOriginalFilename());

        // Placeholder for actual transcription logic (replace this with an API or library call).
        if (audioFile.isEmpty()) {
            throw new IOException("Audio file is empty.");
        }

        // Simulate transcription process (replace with actual API/service logic).
        return "Transcribed text from audio: " + audioFile.getOriginalFilename();
    }
}
