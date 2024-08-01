package com.springboot.FlomadAIplanner.ui.voice;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.springboot.FlomadAIplanner.ai.ChatBot;


public class VoiceHandler {

    private final WebEngine webEngine;
    private TextArea chatArea;
    private TextField userInputField; 
    private VoiceHandler voiceHandler;
    private ChatBot chatBot;
    private static final Logger logger = Logger.getLogger(VoiceHandler.class.getName());

    public void start(Stage primaryStage) {
        // Start the voice recognition service or initialize any required resources here
        // For example, you might need to load a voice recognition model or create a microphone input stream
        // Once the service is started, call startVoiceRecognition() to start listening for speech input
        // Note: This method should be called from the JavaFX Application Thread (JavaFX Platform Thread)
        // Otherwise, you may encounter issues with thread synchronization and UI updates
        // You can use JavaFX's Platform.runLater() method to run the startVoiceRecognition() method on the JavaFX Platform Thread
        // Example: Platform.runLater(() -> startVoiceRecognition());
        primaryStage.setTitle("Flomad: Conversational AI ChatBot");
        chatArea = new TextArea();
        chatArea.setEditable(false);
        chatArea.setPrefHeight(200);

        userInputField = new TextField();
        userInputField.setPromptText("Ask a question...");
        userInputField.setOnAction(event -> handleUserInput());

        Button voiceButton = new Button("Activate Voice");
        voiceHandler = new VoiceHandler(webEngine);
        voiceButton.setOnAction(event -> voiceHandler.startVoiceRecognition());
 
        // Create the Physical View of the ChatBot! 
        WebView webView = new WebView();
        WebEngine webEngine = webView.getEngine();
        webEngine.load(getClass().getResource("/voice/voice.html").toExternalForm());
        webEngine.getLoadWorker().stateProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == javafx.concurrent.Worker.State.SUCCEEDED) {
                webEngine.executeScript("window.java = window.java || {}; window.java.receiveSpeechInput = function(text) { javafx.receiveSpeechInput(text); };");
            }
        });
        
        VBox vbox = new VBox(10, chatArea, userInputField, voiceButton, webView);
        Scene scene = new Scene(vbox, 400, 600);
        primaryStage.setScene(scene);
        primaryStage.show();

    }

    private void handleUserInput() {
        String userInput = userInputField.getText();
        if (userInput.isEmpty()) {
            return;
        }

        chatArea.appendText("You: " + userInput + "\n");
        userInputField.clear();

        new Thread(() -> {
            try {
                String response = chatBot.getResponse(userInput);
                chatArea.appendText("AI: " + response + "\n");
                voiceHandler.speakText(response);
            } catch (IOException e) {
                e.printStackTrace();
                chatArea.appendText("AI: Error fetching response\n");
            }
        }).start();
    }
    
    public VoiceHandler(WebEngine webEngine) {
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
        logger.log(Level.INFO, "Recognized speech: " + speechInput);
        // Handle the speech input (e.g., send it to the ChatBot)
    }
}

