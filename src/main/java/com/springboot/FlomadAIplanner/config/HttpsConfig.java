package com.springboot.FlomadAIplanner.config;

import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile({"dev", "prod"})
public class HttpsConfig {

    @Bean
    public WebServerFactoryCustomizer<TomcatServletWebServerFactory> servletContainer() {
        return factory -> {
            factory.addAdditionalTomcatConnectors();
            // Configure Tomcat to properly handle forwarded headers for HTTPS
            factory.addConnectorCustomizers(connector -> {
                connector.setScheme("https");
                connector.setProxyPort(443);
                connector.setSecure(true);
            });
        };
    }
}