package com.springboot.FlomadAIplanner;

import com.springboot.FlomadAIplanner.config.MongoDBConfig;
import com.springboot.FlomadAIplanner.config.PreInitializationConfig;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(classes = FlomadAiPlannerApplication.class)
@Import({MongoDBConfig.class})
@ActiveProfiles("test")
class FlomadAiPlannerApplicationTests {

    @BeforeAll
    static void setUpBeforeClass() {
        // Run the environment variable loading before any tests
        PreInitializationConfig.loadEnvVariables();
    }

    @Test
    void contextLoads() {
        // Your test code here
    }
}
