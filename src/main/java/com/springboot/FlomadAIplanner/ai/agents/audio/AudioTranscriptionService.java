package com.springboot.FlomadAIplanner.ai.agents.audio;

import org.springframework.ai.audio.transcription.AudioTranscriptionPrompt;
import org.springframework.ai.audio.transcription.AudioTranscriptionResponse;
import org.springframework.ai.model.Model;
import org.springframework.ai.openai.OpenAiAudioTranscriptionOptions;
import org.springframework.ai.openai.api.OpenAiAudioApi.TranscriptResponseFormat;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
public class AudioTranscriptionService {

    private final Model<AudioTranscriptionPrompt, AudioTranscriptionResponse> transcriptionModel;

    @Value("classpath:/templates/SimpleTranscriptionPrompt.txt")
    private String transcriptionPrompt;

    public AudioTranscriptionService(@Qualifier("openAiAudioTranscriptionModel") Model<AudioTranscriptionPrompt, AudioTranscriptionResponse> transcriptionModel) {
        this.transcriptionModel = transcriptionModel;
    }

    public String transcribeAudio(MultipartFile audioFile) throws IOException {
        Resource audioResource = convertMultipartFileToResource(audioFile);
        OpenAiAudioTranscriptionOptions transcriptionOptions = OpenAiAudioTranscriptionOptions.builder()
                .prompt(transcriptionPrompt)
                .responseFormat(TranscriptResponseFormat.TEXT)
                .build();
        AudioTranscriptionPrompt prompt = new AudioTranscriptionPrompt(audioResource, transcriptionOptions);
        AudioTranscriptionResponse response = transcriptionModel.call(prompt);
        if (response == null || response.getResults().isEmpty()) {
            throw new IOException("Transcription response is empty.");
        }
        return response.getResults().get(0).getOutput().trim();
    }

    private Resource convertMultipartFileToResource(MultipartFile multipartFile) throws IOException {
        return new ByteArrayResource(multipartFile.getBytes()) {
            @Override
            public String getFilename() {
                return multipartFile.getOriginalFilename();
            }
        };
    }
}
