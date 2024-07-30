package com.springboot.FlomadAIplanner.converter;

import com.springboot.FlomadAIplanner.ai.functions.FlightSearchFunction;
import com.springboot.FlomadAIplanner.dto.amadeus.FlightOffersPostResponse;
import com.springboot.FlomadAIplanner.dto.amadeus.FlightOffersPostResponse.FlightOffer;
import com.springboot.FlomadAIplanner.dto.amadeus.FlightOffersPostResponse.Segment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
public class FlightOffersPostResponseToFlightSearchResponseConverter implements Converter<FlightOffersPostResponse, FlightSearchFunction.FlightSearchResponse> {

    /**
     * Converts the FlightOffersSearchResponse to FlightSearchResponse
     * <p> The FlightOffersSearchResponse is the response from the Amadeus API
     * and the FlightSearchResponse is the response that will be sent to the LLM
     *
     * @param source the FlightOffersSearchResponse
     * @return the FlightSearchResponse
     */
    @Override
    public FlightSearchFunction.FlightSearchResponse convert(FlightOffersPostResponse source) {
        log.info("Converting FlightOffersSearchResponse to FlightSearchResponse");
        List<FlightSearchFunction.FlightDetail> flights = new ArrayList<>();

        Optional.ofNullable(source.getData()).ifPresent(dataList ->
                dataList.forEach(flightOffer -> {
                    String price = getPrice(flightOffer);

                    Optional.ofNullable(flightOffer.getItineraries()).ifPresent(itineraries ->
                            itineraries.forEach(itinerary ->
                                    Optional.ofNullable(itinerary.getSegments()).ifPresent(segments ->
                                            segments.forEach(segment -> flights.add(createFlightDetail(segment, price, source)))
                                    )
                            )
                    );
                })
        );
        log.info("Converted FlightOffersSearchResponse to FlightSearchResponse");

        return new FlightSearchFunction.FlightSearchResponse(flights);
    }

    private String getPrice(FlightOffer flightOffer) {
        return Optional.ofNullable(flightOffer.getPrice())
                .map(FlightOffersPostResponse.Price::getTotal)
                .orElse("0.0");
    }

    private FlightSearchFunction.FlightDetail createFlightDetail(Segment segment, String price, FlightOffersPostResponse source) {
        String flightNumber = segment.getCarrierCode() + " " + segment.getNumber();
        String departureTime = Optional.ofNullable(segment.getDeparture())
                .map(FlightOffersPostResponse.FlightEndPoint::getAt)
                .orElse("");
        String arrivalTime = Optional.ofNullable(segment.getArrival())
                .map(FlightOffersPostResponse.FlightEndPoint::getAt)
                .orElse("");
        String airline = Optional.ofNullable(source.getDictionaries())
                .map(dictionaries -> dictionaries.getCarriers().getCarrierName())
                .orElse("");
        String bookingLink = "https://booking.example.com";

        return new FlightSearchFunction.FlightDetail(flightNumber, departureTime, arrivalTime, airline, Double.parseDouble(price), bookingLink);
    }
}
