package com.springboot.FlomadAIplanner;

import com.springboot.FlomadAIplanner.config.MongoDBConfig;
import com.springboot.FlomadAIplanner.config.PreInitializationConfig;
import com.springboot.FlomadAIplanner.config.TestPositionStackConfig;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

/**
 * Full context load requires MongoDB (default localhost:27017; or set MONGODB_TEST_PORT=27018).
 * Excluded from default test run via @Tag("integration"); run with: mvn test -DexcludedGroups=
 */
@Tag("integration")
@SpringBootTest(classes = FlomadAiPlannerApplication.class)
@Import({MongoDBConfig.class, TestPositionStackConfig.class})
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
