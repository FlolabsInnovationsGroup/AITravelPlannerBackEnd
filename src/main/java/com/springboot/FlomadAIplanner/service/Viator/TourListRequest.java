package com.springboot.FlomadAIplanner.service.Viator;
import java.time.LocalDateTime;

public class TourListRequest {
    private String requestType;
    private Data data;

    // getters and setters
    public String getRequestType() {
        return requestType;
    }

    public void setRequestType(String requestType) {
        this.requestType = requestType;
    }

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    public static class Data {
        private String apiKey;
        private int resellerId;
        private int supplierId;
        private String externalReference;
        private String timestamp;

        // getters and setters
        public String getApiKey() {
            return apiKey;
        }

        public void setApiKey(String apiKey) {
            this.apiKey = apiKey;
        }

        public int getResellerId() {
            return resellerId;
        }

        public void setResellerId(int resellerId) {
            this.resellerId = resellerId;
        }

        public int getSupplierId() {
            return supplierId;
        }

        public void setSupplierId(int supplierId) {
            this.supplierId = supplierId;
        }

        public String getExternalReference() {
            return externalReference;
        }

        public void setExternalReference(String externalReference) {
            this.externalReference = externalReference;
        }

        public String getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(String timestamp) {
            this.timestamp = timestamp;
        }

    }
}
