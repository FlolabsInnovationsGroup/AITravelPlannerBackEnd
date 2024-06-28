package com.springboot.FlomadAIplanner.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TourListRequest {
    private String requestType;
    private Data data;

    @lombok.Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Data {
        private int resellerId;
        private int supplierId;
        private String externalReference;
        private String timestamp;
    }
}

