package com.springboot.FlomadAIplanner.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.testcontainers.containers.MongoDBContainer;

import java.util.Map;

@Slf4j
@TestConfiguration
public class MongoDBConfig implements ApplicationListener<ContextRefreshedEvent> {

    private static final String MONGO_URI_PROPERTY = "spring.data.mongodb.uri";

    static MongoDBContainer mongoDBContainer;

    static {
        try {
            log.info("Starting MongoDB Testcontainers (optional - requires Docker)");
            mongoDBContainer = new MongoDBContainer("mongo:4.0.10");
            mongoDBContainer.start();
            log.info("MongoDB container started");
        } catch (Exception e) {
            log.warn("Testcontainers/Docker not available ({}). Tests will use spring.data.mongodb.uri from application-test.properties. Ensure MongoDB is running on that URI.", e.getMessage());
            mongoDBContainer = null;
        }
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        if (mongoDBContainer == null) {
            log.info("Using existing MongoDB URI from configuration (no Testcontainers)");
            return;
        }
        ConfigurableEnvironment environment = (ConfigurableEnvironment) event.getApplicationContext().getEnvironment();
        String mongoUri = mongoDBContainer.getReplicaSetUrl();
        log.info("Setting MongoDB URI dynamically: " + mongoUri);

        MapPropertySource mongoDbPropertySource = new MapPropertySource("mongoDbProperties",
                Map.of(MONGO_URI_PROPERTY, mongoUri));

        environment.getPropertySources().addFirst(mongoDbPropertySource);
        log.info("MongoDB URI set successfully");
    }
}
