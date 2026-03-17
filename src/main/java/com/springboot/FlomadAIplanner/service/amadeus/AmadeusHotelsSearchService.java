package com.springboot.FlomadAIplanner.service.amadeus;

import com.fasterxml.jackson.databind.JsonNode;
import com.springboot.FlomadAIplanner.dto.amadeus.HotelOffersByHotelIdsRequest;
import com.springboot.FlomadAIplanner.dto.amadeus.HotelSearchByGeocodeRequest;
import com.springboot.FlomadAIplanner.dto.amadeus.HotelSearchByGeocodeResponse;
import com.springboot.FlomadAIplanner.exceptions.api.AmadeusAPIException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public final class AmadeusHotelsSearchService {

    /** Amadeus API client. */
    private final AmadeusClient amadeusClient;

    /**
     * Constructor.
     *
     * @param pAmadeusClient Amadeus API client
     */
    public AmadeusHotelsSearchService(final AmadeusClient pAmadeusClient) {
        this.amadeusClient = pAmadeusClient;
    }

    /**
     * Converts a list of strings into a JSON array formatted string.
     *
     * @param list The list of strings to convert
     * @return A string representing a JSON array of quoted strings
     */
    public static String convertListToJsonArray(final List<String> list) {
        return String.join(",", list);
    }

    /**
     * Searches hotels by geocode (lat/lon and radius).
     *
     * @param request search request
     * @return search response
     * @throws AmadeusAPIException on API error
     */
    public HotelSearchByGeocodeResponse searchHotelsByGeocode(
            final HotelSearchByGeocodeRequest request) throws AmadeusAPIException {
        log.info("Searching hotels by geocode lat: {}, lon: {}, radius: {}",
                request.getLatitude(),
                request.getLongitude(),
                request.getRadius());
        try {
            HotelSearchByGeocodeResponse response =
                    amadeusClient.searchHotelsByGeocode(
                            request.getLatitude(),
                            request.getLongitude(),
                            request.getRadius(),
                            request.getRadiusUnit(),
                            request.getChainCodes(),
                            request.getAmenities(),
                            request.getRatings(),
                            request.getHotelSource()
                    );
            log.info("Received hotel search response from Amadeus");
            return response;
        } catch (Exception e) {
            log.error("Error searching for hotels by geocode", e);
            throw new AmadeusAPIException(
                    "Error searching for hotels by geocode", e);
        }
    }

    /**
     * Searches hotel offers by hotel IDs.
     *
     * @param request request with hotel IDs and criteria
     * @return JSON response from Amadeus
     * @throws AmadeusAPIException on API error
     */
    public JsonNode searchHotelOffers(
            final HotelOffersByHotelIdsRequest request)
            throws AmadeusAPIException {
        log.info("Searching hotel offers for hotel IDs: {}",
                request.getHotelIds());
        try {
            JsonNode response = amadeusClient.getMultiHotelOffers(
                    convertListToJsonArray(request.getHotelIds()),
                    request.getAdults(),
                    request.getCheckInDate(),
                    request.getCheckOutDate(),
                    request.getCountryOfResidence(),
                    request.getRoomQuantity(),
                    request.getPriceRange(),
                    request.getCurrency(),
                    request.getPaymentPolicy(),
                    request.getBoardType(),
                    request.getIncludeClosed(),
                    request.getBestRateOnly(),
                    request.getLang()
            );
            log.info("Received hotel offers from Amadeus: {}", response);
            return response;
        } catch (Exception e) {
            log.error("Error searching for hotel offers", e);
            throw new AmadeusAPIException(
                    "Error searching for hotel offers", e);
        }
    }
}
