package com.springboot.FlomadAIplanner.config

import groovy.util.logging.Slf4j
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.ApplicationListener
import org.springframework.context.event.ContextRefreshedEvent
import org.springframework.core.env.ConfigurableEnvironment
import org.springframework.core.env.MapPropertySource
import org.testcontainers.containers.MongoDBContainer

/**
 * Configuration class for setting up MongoDB instance for integration tests.
 * The MongoDB instance is started in a Docker container.
 * The MongoDB URI is set dynamically in the application context.
 *
 * Setting up MongoDB in a Docker container is necessary to avoid using the production database during integration tests.
 */
@Slf4j
@TestConfiguration
class MongoConfig implements ApplicationListener<ContextRefreshedEvent> {

    private static final String MONGO_URI_PROPERTY = "spring.data.mongodb.uri"

    static MongoDBContainer mongoDBContainer

    static {
        log.info("Starting MongoDB container")
        mongoDBContainer = new MongoDBContainer("mongo:4.0.10")
        mongoDBContainer.start()
        log.info("MongoDB container started")
    }

    @Override
    void onApplicationEvent(ContextRefreshedEvent event) {
        ConfigurableEnvironment environment = (ConfigurableEnvironment) event.getApplicationContext().getEnvironment()
        String mongoUri = mongoDBContainer.getReplicaSetUrl()
        log.info("Setting MongoDB URI dynamically: " + mongoUri)

        MapPropertySource mongoDbPropertySource = new MapPropertySource("mongoDbProperties",
                Map.of(MONGO_URI_PROPERTY, mongoUri))

        environment.getPropertySources().addFirst(mongoDbPropertySource)
        log.info("MongoDB URI set successfully")
    }
}
