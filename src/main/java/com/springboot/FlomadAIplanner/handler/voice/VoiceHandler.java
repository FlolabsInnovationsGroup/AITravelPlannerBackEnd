package com.springboot.FlomadAIplanner.handler.voice;


public class VoiceHandler {
    private final WebEngine webEngine;

    public VoiceHandler() {
        this.webEngine = webEngine;
    }

    public void startVoiceRecognition() {
        webEngine.executeScript("window.java.startRecognition()");
    }

    public void speakText(String text) {
        webEngine.executeScript("window.java.speakText('" + text.replace("'", "\\'") + "')");
    }

    // This method will be called from JavaScript
    public void receiveSpeechInput(String speechInput) {
        System.out.println("Recognized speech: " + speechInput);
        // Handle the speech input (e.g., send it to the ChatBot)
    }
}
