package com.springboot.FlomadAIplanner.config

import org.jetbrains.annotations.NotNull
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext

/**
 * Global setup initializer for the integration tests.
 * Before tests are run, this initializer makes necessary global setup.
 * Test class must define this GlobalSetupInitializer class as an initializer in the @ContextConfiguration annotation to run the setup from this class. (to reuse the code if necessary)
 */
class GlobalSetupInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    private static boolean started = false

    @Override
    void initialize(@NotNull ConfigurableApplicationContext applicationContext) {
        if (!started) {
            PreInitializationConfig.loadEnvVariables()
            started = true
        }
    }
}
