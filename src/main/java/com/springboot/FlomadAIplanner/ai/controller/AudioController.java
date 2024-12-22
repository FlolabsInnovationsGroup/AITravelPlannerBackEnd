package com.springboot.FlomadAIplanner.ai.controller;

import com.springboot.FlomadAIplanner.ai.agents.audio.AudioTranscriptionService;
import com.springboot.FlomadAIplanner.ai.dto.AudioTranscribeDTO;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/audio")
public class AudioController {
    private final AudioTranscriptionService audioTranscriptionService;

    public AudioController(AudioTranscriptionService audioTranscriptionService) {
        this.audioTranscriptionService = audioTranscriptionService;
    }

    @Operation(summary = "Process chat message with optional media")
    @PostMapping(value = "/transcribe", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public String handleTranscribe(@ModelAttribute AudioTranscribeDTO request) throws Exception {
        return audioTranscriptionService.transcribeAudio(request.getAudioFile());
    }
}
