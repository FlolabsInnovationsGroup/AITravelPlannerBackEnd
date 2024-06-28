package com.springboot.FlomadAIplanner.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LocationsRequest {
    private String subType;
    private String keyword;
    private String countryCode;
    private String sort;
    private String view;

    public LocationsRequest(String subType, String keyword) {
        this.subType = subType;
        this.keyword = keyword;
    }
}
