package com.springboot.FlomadAIplanner.config.login;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
public class AppConfig {

    @Bean
    @Qualifier("emailExecutorService")
    public ExecutorService executorService() {
        return Executors.newFixedThreadPool(10);
    }
}
