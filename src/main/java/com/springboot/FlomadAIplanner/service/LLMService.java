package com.springboot.FlomadAIplanner.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.springboot.FlomadAIplanner.DTO.MessageDTO;
import com.springboot.FlomadAIplanner.controller.ModelController;

import javax.annotation.PostConstruct;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.logging.Logger;

@Service
public class LLMService {

    private static final Logger logger = Logger.getLogger(LLMService.class.getName());
    
    @Autowired
    private ThymeleafService thymeleafService;

    @Autowired
    private ModelController modelController;

    @Autowired
    private GeminiService geminiService;

    @Autowired
    private OpenAIService openAIService;

    @Value("${llm.timeout.inference}")
    private long timeoutInference;

    @Value("${llm.log.prompts}")
    private boolean logPrompts;

    private final RestTemplate restTemplate = new RestTemplate();

    private final Map<String, String> geminiModels = new HashMap<>();
    private final Map<String, String> openAiModels = new HashMap<>();

    @PostConstruct
    public void init() {
        geminiModels.put("Gemini 1.0 Pro", "gemini-pro");
        // geminiModels.put("gemini", "gemini");


        // openAiModels.put("GPT-4 Turbo", "gpt-4-turbo");
        openAiModels.put("GPT-3.5 Turbo", "gpt-3.5-turbo-0125");
        openAiModels.put("GPT-4 Turbo", "gpt-4-turbo");
        openAiModels.put("GPT-4o", "gpt-4o");

    }

    public Map<String, Map<String, String>> listModels() {
        Map<String, Map<String, String>> models = new HashMap<>();
        models.put("GEMINI", geminiModels);
        models.put("OPENAI", openAiModels);
        return models;
    }

    public String intentRecognition(List<MessageDTO> messages) throws Exception {
        String modelId = modelController.getSelectedModel();
        if (modelId == null) {
            throw new IllegalStateException("No model selected");
        }
        
        String modelType = getModelType(modelId);
        if (modelType == null) {
            throw new IllegalArgumentException("Model " + modelId + " not supported");
        }

        if (messages == null || messages.isEmpty()) {
            return "No messages provided";
        }

        String[] promptArray = new String[messages.size()];
        for (int i = 0; i < messages.size(); i++) {
            promptArray[i] = messages.get(i).getMessage();
        }
        String promptString = String.join(" ", promptArray);
        
        Map<String, Object> variables = new HashMap<>();
        variables.put("sessionMessages", String.join(" ", promptString));
        variables.put("lastUserMessage", messages.get(messages.size() - 1));

        String RequestText = thymeleafService.renderPlainText("IntentPrompt", variables);
        Callable<String> inferenceTask = () -> performInference(modelType, RequestText);
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Future<String> future = executorService.submit(inferenceTask);

        try {
            String response = future.get(timeoutInference, TimeUnit.SECONDS);
            if (logPrompts) {
                System.out.println("Response1: " + response);
            }
            return response;
        } catch (TimeoutException e) {
            throw new Exception("intentRecognition took too long. Please try again.", e);
        } catch (Exception e) {
            throw new Exception("intentRecognition failed.", e);
        } finally {
            executorService.shutdown();
        }
    }


    public String inference(List<MessageDTO> prompt, String propmptTemplate) throws Exception {
        String modelId = modelController.getSelectedModel();
        if (modelId == null) {
            throw new IllegalStateException("No model selected");
        }
        
        String modelType = getModelType(modelId);
        if (modelType == null) {
            throw new IllegalArgumentException("Model " + modelId + " not supported");
        }

        String[] promptArray = new String[prompt.size()];
        for (int i = 0; i < prompt.size(); i++) {
            promptArray[i] = prompt.get(i).getMessage();
        }
        String promptString = String.join(" ", promptArray);
        Map<String, Object> variables = new HashMap<>();
        variables.put("sessionMessages", String.join(" ", promptString));
        variables.put("lastUserMessage", promptArray[prompt.size() - 1]);

        String RequestText = thymeleafService.renderPlainText(propmptTemplate, variables);

        Callable<String> inferenceTask = () -> performInference(modelType, RequestText);
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Future<String> future = executorService.submit(inferenceTask);

        try {
            String response = future.get(timeoutInference, TimeUnit.SECONDS);
            if (logPrompts) {
                System.out.println("Response2: " + response);
            }
            return response;
        } catch (TimeoutException e) {
            throw new Exception("Inference took too long. Please try again.", e);
        } catch (Exception e) {
            throw new Exception("Inference failed.", e);
        } finally {
            executorService.shutdown();
        }
    }

    private String performInference(String modelType, String prompt) throws IOException {
        String temp; // Declare the variable 'temp'
        if (modelType.equals("GEMINI")) {
            return this.geminiService.sendMessageToGemini(prompt);
            // return restTemplate.postForObject("https://api.gemini.ai/v1/inference", prompt, String.class);
        } else if (modelType.equals("OPENAI")) {
            temp = this.openAIService.sendMessageToOpenAI(prompt); // Assign a value to 'temp'
            logger.info("Response from OpenAI: " + temp); // Log the response from OpenAI
            return temp; // Return 'temp'
            // return restTemplate.postForObject("https://api.openai.com/v1/inference", prompt, String.class);
        }
        return null;
    }

    private String getModelType(String modelId) {
        if (geminiModels.containsKey(modelId)) {
            return "GEMINI";
        } else if (openAiModels.containsKey(modelId)) {
            return "OPENAI";
        }
        return null;
    }
}
