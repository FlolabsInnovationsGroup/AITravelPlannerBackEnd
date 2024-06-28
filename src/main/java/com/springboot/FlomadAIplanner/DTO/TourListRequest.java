package com.springboot.FlomadAIplanner.DTO;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Data
public class TourListRequest {
    private String requestType;
    private Data data;

    @Getter
    @Setter
    @ToString
    public static class Data {
        private String apiKey;
        private int resellerId;
        private int supplierId;
        private String externalReference;
        private String timestamp;

    }
}
