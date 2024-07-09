package com.springboot.FlomadAIplanner.ai.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class PromptsConfig {

    @Bean
    public Map<String, String> prompts(ResourceLoader resourceLoader) throws IOException {
        Map<String, String> prompts = new HashMap<>();
        String[] promptFiles = {
                "classpath:/templates/FunctionsSuggestionsPrompt.txt",
                "classpath:/templates/SingleQuestionPrompt.txt",
                "classpath:/templates/promptAndFunctionSuggestionsPrompt.txt",
                "classpath:/templates/WithFunctionsQuestionPrompt.txt",
                "classpath:/templates/DefaultRulesPrompt.txt",
                "classpath:/templates/InternetSearchRulesPrompt.txt",
                "classpath:/templates/FallbackPrompt.txt"
        };

        for (String promptFile : promptFiles) {
            Resource resource = resourceLoader.getResource(promptFile);
            String content = Files.readString(Path.of(resource.getURI()));
            String fileName = resource.getFilename();
            prompts.put(fileName, content);
        }

        return prompts;
    }
}

