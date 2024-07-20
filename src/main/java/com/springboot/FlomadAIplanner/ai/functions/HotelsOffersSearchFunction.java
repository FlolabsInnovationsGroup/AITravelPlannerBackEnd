package com.springboot.FlomadAIplanner.ai.functions;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.springboot.FlomadAIplanner.dto.amadeus.HotelOffersByHotelIdsRequest;
import com.springboot.FlomadAIplanner.exceptions.api.AmadeusAPIException;
import com.springboot.FlomadAIplanner.service.amadeus.AmadeusHotelsSearch;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@Description("""
        Use this function to search for multiple hotels' offers at ones, by a predefined list of hotel IDs and other
        optional parameters such as check-in and check-out dates, number of adults, room quantity, and more.
        This function allows users to specify exact hotels' properties
        and refine their search with various options like price range, board type, and amenities.
        This method is ideal for users who know exactly what they want and are looking to tailor their hotel search to fit specific requirements.
        Mostly used to check the availability of the hotel apartments/rooms/beds by a specific filter request.
        Use CurrentDateTimeFunction to get the current date and time before calling this function if you need to use date parameter.
        You can USE this function to search for offers in MULTIPLE hotels AT ONES using this function for more efficiency.
        """)
public class HotelsOffersSearchFunction implements AIFunction<HotelsOffersSearchFunction.HotelOffersRequestWrapper, JsonNode> {

    private final AmadeusHotelsSearch amadeusHotelsSearch;

    @Autowired
    public HotelsOffersSearchFunction(AmadeusHotelsSearch amadeusHotelsSearch) {
        this.amadeusHotelsSearch = amadeusHotelsSearch;
    }

    @Override
    @Valid
    public JsonNode apply(HotelOffersRequestWrapper requestWrapper) {
        log.info("Initiating hotel offers search for multiple hotel IDs.");
        ArrayNode combinedResults = JsonNodeFactory.instance.arrayNode();

        for (String hotelId : requestWrapper.getRequest().getHotelIds()) {
            try {
                HotelOffersByHotelIdsRequest singleHotelRequest = new HotelOffersByHotelIdsRequest(
                        List.of(hotelId),  // Single hotel ID
                        requestWrapper.getRequest().getAdults(),
                        requestWrapper.getRequest().getCheckInDate(),
                        requestWrapper.getRequest().getCheckOutDate(),
                        requestWrapper.getRequest().getCountryOfResidence(),
                        requestWrapper.getRequest().getRoomQuantity(),
                        requestWrapper.getRequest().getPriceRange(),
                        requestWrapper.getRequest().getCurrency(),
                        requestWrapper.getRequest().getPaymentPolicy(),
                        requestWrapper.getRequest().getBoardType(),
                        requestWrapper.getRequest().getIncludeClosed(),
                        requestWrapper.getRequest().getBestRateOnly(),
                        requestWrapper.getRequest().getLang()
                );

                log.info("Searching offers for hotel ID: {}", hotelId);
                JsonNode response = amadeusHotelsSearch.searchHotelOffers(singleHotelRequest);
                combinedResults.add(response);
            } catch (AmadeusAPIException e) {
                log.error("Error searching for hotel offers for hotel ID: {}", hotelId);
                JsonNode errorNode = JsonNodeFactory.instance.objectNode()
                        .put("error", "Error for hotel ID " + hotelId + ": " + e.getMessage());
                combinedResults.add(errorNode);
            }
        }

        log.info("Combined search results for all provided hotel IDs.");
        return combinedResults;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HotelOffersRequestWrapper {

        @Valid
        @JsonPropertyDescription("The hotel offers search request body containing hotel IDs and other filter parameters.")
        private HotelOffersByHotelIdsRequest request;
    }
}
