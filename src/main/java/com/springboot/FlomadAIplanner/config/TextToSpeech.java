package com.springboot.FlomadAIplanner.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.texttospeech.v1.*;
import com.google.protobuf.ByteString;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class TextToSpeech {

    public static void main(String[] args) {
        try {
            // Set up the client with the service account key file
            GoogleCredentials credentials = GoogleCredentials.fromStream(new FileInputStream("path/to/your/service-account-file.json"));
            TextToSpeechSettings settings = TextToSpeechSettings.newBuilder().setCredentialsProvider(() -> credentials).build();
            TextToSpeechClient textToSpeechClient = TextToSpeechClient.create(settings);

            // Configure the request with required parameters
            SynthesisInput input = SynthesisInput.newBuilder().setText("Hello, world!").build();
            VoiceSelectionParams voice = VoiceSelectionParams.newBuilder()
                    .setLanguageCode("en-US")
                    .setSsmlGender(SsmlVoiceGender.NEUTRAL)
                    .build();
            AudioConfig audioConfig = AudioConfig.newBuilder()
                    .setAudioEncoding(AudioEncoding.MP3)
                    .build();

            // Perform text-to-speech conversion
            SynthesizeSpeechResponse response = textToSpeechClient.synthesizeSpeech(input, voice, audioConfig);

            // Get the audio content from the response
            ByteString audioContents = response.getAudioContent();

            // Write the audio content to a file
            try (OutputStream out = new FileOutputStream("output.mp3")) {
                out.write(audioContents.toByteArray());
                System.out.println("Audio content written to file 'output.mp3'");
            }

            // Close the client
            textToSpeechClient.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
