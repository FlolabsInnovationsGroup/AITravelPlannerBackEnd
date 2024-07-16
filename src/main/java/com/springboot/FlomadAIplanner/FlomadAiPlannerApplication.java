package com.springboot.FlomadAIplanner;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class FlomadAiPlannerApplication {

    public static void main(String[] args) {
        SpringApplication.run(FlomadAiPlannerApplication.class, args);
    }

}
