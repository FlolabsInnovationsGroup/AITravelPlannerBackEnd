package com.springboot.FlomadAIplanner;

import com.springboot.FlomadAIplanner.config.PreInitializationConfig;
import io.mongock.runner.springboot.EnableMongock;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@Slf4j
@SpringBootApplication
@EnableMongock
@EnableFeignClients
public class FlomadAiPlannerApplication {
    public static void main(String[] args) {
        PreInitializationConfig.loadEnvVariables();
        SpringApplication.run(FlomadAiPlannerApplication.class, args);
    }

}