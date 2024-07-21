package com.springboot.FlomadAIplanner.ai.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static java.nio.file.Files.readString;

@Configuration
public class PromptsConfig {

    // Info related to these delimiters are included in PromptAndFunctionSuggestionsPrompt.txt file,
    // so it is important to keep them in sync.
    private static final String OPEN_DELIMITER = "[[[";
    private static final String CLOSE_DELIMITER = "]]]";

    /**
     * This method reads the content of the files in the templates folder and returns a map of the file name and the content.
     * The content is wrapped in delimiters to be used in the chat client.
     *
     * @return a map of the file name and the content
     * @throws IOException if an I/O error occurs
     */
    @Bean(name = "templatePrompts")
    public Map<String, String> prompts() throws IOException {
        Map<String, String> prompts = new HashMap<>();
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();

        // Assuming the templates are directly under resources folder
        Resource[] resources = resolver.getResources("classpath:/templates/general-instructions-variants/.");

        for (Resource resource : resources) {
            String content = new BufferedReader(new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))
                    .lines()
                    .collect(Collectors.joining("\n"));
            content = OPEN_DELIMITER.concat(content).concat(CLOSE_DELIMITER);

            String fileName = resource.getFilename();

            prompts.put(fileName, content);
        }

        return prompts;
    }
}

