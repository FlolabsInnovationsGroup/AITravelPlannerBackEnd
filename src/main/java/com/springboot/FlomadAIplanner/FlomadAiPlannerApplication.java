package com.springboot.FlomadAIplanner;

import com.springboot.FlomadAIplanner.config.PreInitializationConfig;
import io.mongock.runner.springboot.EnableMongock;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.openfeign.EnableFeignClients;
// import org.springframework.scheduling.annotation.EnableScheduling;  // TEMPORARILY DISABLED - causing JAR corruption

@Slf4j
@SpringBootApplication(exclude = {
    org.springframework.ai.autoconfigure.vertexai.gemini.VertexAiGeminiAutoConfiguration.class
})
@EnableMongock
@EnableFeignClients
@EnableCaching
// @EnableScheduling  // TEMPORARILY DISABLED - will re-enable after fixing dependency conflict
public class FlomadAiPlannerApplication {
    public static void main(String[] args) {
        PreInitializationConfig.loadEnvVariables();
        SpringApplication.run(FlomadAiPlannerApplication.class, args);
    }

}