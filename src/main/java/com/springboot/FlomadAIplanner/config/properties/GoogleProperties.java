package com.springboot.FlomadAIplanner.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "google")
public class GoogleProperties {

    private final Api api = new Api();
    private final Places places = new Places();
    private final TextSearch textSearch = new TextSearch();

    public Api getApi() {
        return api;
    }

    public Places getPlaces() {
        return places;
    }

    public TextSearch getTextSearch() {
        return textSearch;
    }

    public static class Api {
        private String key;
        private String baseurl;

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public String getBaseurl() {
            return baseurl;
        }

        public void setBaseurl(String baseurl) {
            this.baseurl = baseurl;
        }
    }

    public static class Places {
        private String fieldMask;
        private int maxResults = 20;

        public String getFieldMask() {
            return fieldMask;
        }

        public void setFieldMask(String fieldMask) {
            this.fieldMask = fieldMask;
        }

        public int getMaxResults() {
            return maxResults;
        }

        public void setMaxResults(int maxResults) {
            this.maxResults = maxResults;
        }
    }

    public static class TextSearch {
        private String fieldMask;

        public String getFieldMask() {
            return fieldMask;
        }

        public void setFieldMask(String fieldMask) {
            this.fieldMask = fieldMask;
        }
    }
}