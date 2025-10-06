package com.springboot.FlomadAIplanner.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "application")
public class ApplicationProperties {

    private final Cors cors = new Cors();
    private final Email email = new Email();
    private final FrontEnd frontEnd = new FrontEnd();
    private final Backend backend = new Backend();
    private int guestMessageLimit = 1000;

    public Cors getCors() {
        return cors;
    }

    public Email getEmail() {
        return email;
    }

    public FrontEnd getFrontEnd() {
        return frontEnd;
    }

    public Backend getBackend() {
        return backend;
    }

    public int getGuestMessageLimit() {
        return guestMessageLimit;
    }

    public void setGuestMessageLimit(int guestMessageLimit) {
        this.guestMessageLimit = guestMessageLimit;
    }

    public static class Cors {
        private final Allowed allowed = new Allowed();
        private final Allow allow = new Allow();

        public Allowed getAllowed() {
            return allowed;
        }

        public Allow getAllow() {
            return allow;
        }

        public static class Allowed {
            private String origins;
            private String methods;
            private String headers;

            public String getOrigins() {
                return origins;
            }

            public void setOrigins(String origins) {
                this.origins = origins;
            }

            public String getMethods() {
                return methods;
            }

            public void setMethods(String methods) {
                this.methods = methods;
            }

            public String getHeaders() {
                return headers;
            }

            public void setHeaders(String headers) {
                this.headers = headers;
            }
        }

        public static class Allow {
            private boolean credentials = true;

            public boolean isCredentials() {
                return credentials;
            }

            public void setCredentials(boolean credentials) {
                this.credentials = credentials;
            }
        }
    }

    public static class Email {
        private final Confirmation confirmation = new Confirmation();

        public Confirmation getConfirmation() {
            return confirmation;
        }

        public static class Confirmation {
            private boolean enabled = false;

            public boolean isEnabled() {
                return enabled;
            }

            public void setEnabled(boolean enabled) {
                this.enabled = enabled;
            }
        }
    }

    public static class FrontEnd {
        private String url;

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }
    }

    public static class Backend {
        private String url;

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }
    }
}