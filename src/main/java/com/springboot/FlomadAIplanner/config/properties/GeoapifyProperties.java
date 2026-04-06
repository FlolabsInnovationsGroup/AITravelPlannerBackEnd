package com.springboot.FlomadAIplanner.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "geoapify.api")
public class GeoapifyProperties {
    private String key;
    private String baseurl;

    public String getKey() { return key; }
    public void setKey(String key) { this.key = key; }
    public String getBaseurl() { return baseurl; }
    public void setBaseurl(String baseurl) { this.baseurl = baseurl; }
}
