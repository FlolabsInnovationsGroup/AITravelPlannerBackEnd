package com.springboot.FlomadAIplanner.handler.voice;


public class VoiceHandler {
    private WebEngine webEngine;

    public VoiceHandler() {
    }

    public void startVoiceRecognition() {
        webEngine.executeScript();
    }

    public void speakText(String text) {
        webEngine.executeScript();
    }

    // This method will be called from JavaScript
    public void receiveSpeechInput(String speechInput) {
        System.out.println("Recognized speech: " + speechInput);
        // Handle the speech input (e.g., send it to the ChatBot)
    }
}
