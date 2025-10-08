package com.springboot.FlomadAIplanner.config;

import com.springboot.FlomadAIplanner.config.properties.*;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({
    ApplicationProperties.class,
    GoogleProperties.class,
    JwtProperties.class,
    ChatMemoryProperties.class,
    ViatorProperties.class,
    AmadeusProperties.class,
    FreeCurrencyProperties.class,
    PositionStackProperties.class
})
public class PropertiesConfiguration {
    // This class enables the configuration properties for custom application settings
}