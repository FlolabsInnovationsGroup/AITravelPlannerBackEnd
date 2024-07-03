package com.springboot.FlomadAIplanner.ai.functions;

import com.fasterxml.jackson.annotation.JsonClassDescription;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@Description("Searches for available hotels based on the provided criteria.")
public class HotelSearchFunctionMock implements AIFunction<HotelSearchFunctionMock.HotelSearchRequest, HotelSearchFunctionMock.HotelSearchResponse> {

    @Override
    public HotelSearchResponse apply(HotelSearchRequest request) {
        log.info("Searching for hotels in {} from {} to {} for {} guests", request.getCity(), request.getCheckInDate(), request.getCheckOutDate(), request.getGuests());
        // Mock implementation
        return new HotelSearchResponse(List.of(
                new HotelDetail("Hotel de Crillon", "10 Place de la Concorde, 75008 Paris, France", 500, List.of(1, 2), "https://www.rosewoodhotels.com/-/media/images/hotels/crillon/paris/rosewood-paris-hotel-de-crillon-lobby-entrance.jpg"),
                new HotelDetail("Four Seasons Hotel George V", "31 Avenue George V, 75008 Paris, France", 600, List.of(3), "https://www.fourseasons.com/content/dam/fourseasons/images/web/PAR/PAR_880_aspect16x9.jpg/jcr:content/renditions/cq5dam.web.1280.1280.jpeg")
        ));
    }

    @Data
    public static class HotelSearchRequest {
        private String city;
        private String checkInDate;
        private String checkOutDate;
        private int guests;
    }

    @Data
    public static class HotelSearchResponse {
        private final List<HotelDetail> hotels;
    }

    @Data
    public static class HotelDetail {
        private final String name;
        private final String address;
        private final double price;
        private final List<Integer> daysToStay;
        private final String bookingLink;
    }
}
