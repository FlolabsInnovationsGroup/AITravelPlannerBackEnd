package com.springboot.FlomadAIplanner;

import io.github.cdimascio.dotenv.Dotenv;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@Slf4j
@SpringBootApplication
@EnableFeignClients
public class FlomadAiPlannerApplication {
    public static void main(String[] args) {
        loadEnvVariables();
        SpringApplication.run(FlomadAiPlannerApplication.class, args);
    }

    private static void loadEnvVariables() {
        log.info("Loading environment variables from .env file");
        Dotenv dotenv = Dotenv.load();

        dotenv.entries().forEach(entry -> {
            String key = entry.getKey();
            String value = entry.getValue();
            System.setProperty(key, value);
        });
        log.info("Environment variables loaded successfully.");
    }

}