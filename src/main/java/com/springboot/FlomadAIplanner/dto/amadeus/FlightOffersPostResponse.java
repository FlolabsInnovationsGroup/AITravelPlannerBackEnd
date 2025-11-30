package com.springboot.FlomadAIplanner.dto.amadeus;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.springboot.FlomadAIplanner.util.ToStringUtil;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class FlightOffersPostResponse {

    @JsonPropertyDescription("Warnings related to the request.")
    @Builder.Default
    private List<Issue> warnings = new ArrayList<>();

    // @JsonPropertyDescription("Metadata for the collection.")
    // private CollectionMeta meta;

    @JsonPropertyDescription("List of flight offers.")
    private List<FlightOffer> data;

    @JsonPropertyDescription("Dictionaries for interpreting the response.")
    private Dictionaries dictionaries;

    @Override
    public String toString() {
        return ToStringUtil.toStringNonNullOnly(this);
    }

    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Issue {
        @JsonPropertyDescription("HTTP status code applicable to this error.")
        private int status;

        @JsonPropertyDescription("Application-specific error code.")
        private long code;

        @JsonPropertyDescription("Short summary of the error.")
        private String title;

        @JsonPropertyDescription("Explanation of the error.")
        private String detail;

        @JsonPropertyDescription("References to the source of the error.")
        private IssueSource source;
    }

    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class IssueSource {
        @JsonPropertyDescription("A JSON Pointer to the associated entity in the request document.")
        private String pointer;

        @JsonPropertyDescription("A string indicating which URI query parameter caused the issue.")
        private String parameter;

        @JsonPropertyDescription("A string indicating an example of the right value.")
        private String example;
    }

    // @Data
    // @JsonInclude(JsonInclude.Include.NON_NULL)
    // @JsonIgnoreProperties(ignoreUnknown = true)
    // public static class CollectionMeta {
    //     @JsonPropertyDescription("Total number of items in the collection.")
    //     private int count;

    //     @JsonPropertyDescription("List of one-way combinations.")
    //     private List<OneWayCombination> oneWayCombinations;
    // }

    // @Data
    // @JsonInclude(JsonInclude.Include.NON_NULL)
    // @JsonIgnoreProperties(ignoreUnknown = true)
    // public static class OneWayCombination {
    //     @JsonPropertyDescription("ID of the origin-destination.")
    //     private String originDestinationId;

    //     @JsonPropertyDescription("List of flight offer IDs.")
    //     private List<String> flightOfferIds;
    // }

    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class FlightOffer {
        @JsonPropertyDescription("The resource name.")
        private String type;

        @JsonPropertyDescription("ID of the flight offer.")
        private String id;

        @JsonPropertyDescription("Source of the flight offer.")
        private String source;

        // @JsonPropertyDescription("If true, a ticketing will be required at booking step.")
        // private boolean instantTicketingRequired;

        // @JsonPropertyDescription("If true, allows to book a PNR without pricing.")
        // private boolean disablePricing;

        // @JsonPropertyDescription("If true, the pricing solution is expected to yield multiple records.")
        // private boolean nonHomogeneous;

        @JsonPropertyDescription("If true, the flight offer can be combined with other one-way flight offers.")
        private boolean oneWay;

        // @JsonPropertyDescription("If true, a payment card is mandatory to book this flight offer.")
        // private boolean paymentCardRequired;

        @JsonPropertyDescription("The last date for ticketing.")
        private String lastTicketingDate;

        // @JsonPropertyDescription("The last date and time for ticketing.")
        // private String lastTicketingDateTime;

        @JsonPropertyDescription("Number of seats bookable in a single request.")
        private int numberOfBookableSeats;

        @JsonPropertyDescription("List of itineraries for this flight offer.")
        private List<Itinerary> itineraries;

        @JsonPropertyDescription("Price information for this flight offer.")
        private Price price;

        // @JsonPropertyDescription("Pricing options for this flight offer.")
        // private PricingOptions pricingOptions;

        @JsonPropertyDescription("List of validating airline codes.")
        private List<String> validatingAirlineCodes;

        // @JsonPropertyDescription("Fare information for each traveler/segment.")
        // private List<TravelerPricing> travelerPricings;
    }

    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Itinerary {
        @JsonPropertyDescription("Duration of the itinerary in ISO8601 format.")
        private String duration;

        @JsonPropertyDescription("List of segments in this itinerary.")
        private List<Segment> segments;
    }

    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Segment {
        @JsonPropertyDescription("ID of the segment.")
        private String id;

        @JsonPropertyDescription("Number of stops planned on the segment.")
        private int numberOfStops;

        // @JsonPropertyDescription("If true, the flight is blacklisted in the EU.")
        // private boolean blacklistedInEU;

        // @JsonPropertyDescription("List of CO2 emissions for this segment.")
        // private List<Co2Emission> co2Emissions;

        @JsonPropertyDescription("Departure information.")
        private FlightEndPoint departure;

        @JsonPropertyDescription("Arrival information.")
        private FlightEndPoint arrival;

        @JsonPropertyDescription("Airline/carrier code.")
        private String carrierCode;

        @JsonPropertyDescription("Flight number.")
        private String number;

        // @JsonPropertyDescription("Information related to the aircraft.")
        // private AircraftEquipment aircraft;

        // @JsonPropertyDescription("Information about the operating flight.")
        // private OperatingFlight operating;

        @JsonPropertyDescription("Stop duration in ISO8601 format.")
        private String duration;

        // @JsonPropertyDescription("List of stops in this segment.")
        // private List<FlightStop> stops;
    }

    // @Data
    // @JsonInclude(JsonInclude.Include.NON_NULL)
    // @JsonIgnoreProperties(ignoreUnknown = true)
    // public static class Co2Emission {
    //     @JsonPropertyDescription("Weight of CO2 emitted for this segment.")
    //     private int weight;

    //     @JsonPropertyDescription("Unit of weight (KG or LB).")
    //     private String weightUnit;

    //     @JsonPropertyDescription("Quality of service offered in the cabin (ECONOMY, PREMIUM_ECONOMY, BUSINESS, FIRST).")
    //     private String cabin;
    // }

    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class FlightEndPoint {
        @JsonPropertyDescription("IATA airline code.")
        private String iataCode;

        @JsonPropertyDescription("Terminal name/number.")
        private String terminal;

        @JsonPropertyDescription("Local date and time in ISO8601 format.")
        private String at;
    }

    // @Data
    // @JsonInclude(JsonInclude.Include.NON_NULL)
    // @JsonIgnoreProperties(ignoreUnknown = true)
    // public static class AircraftEquipment {
    //     @JsonPropertyDescription("IATA aircraft code.")
    //     private String code;
    // }

    // @Data
    // @JsonInclude(JsonInclude.Include.NON_NULL)
    // @JsonIgnoreProperties(ignoreUnknown = true)
    // public static class OperatingFlight {
    //     @JsonPropertyDescription("Airline/carrier code.")
    //     private String carrierCode;
    // }

    // @Data
    // @JsonInclude(JsonInclude.Include.NON_NULL)
    // @JsonIgnoreProperties(ignoreUnknown = true)
    // public static class FlightStop {
    //     @JsonPropertyDescription("IATA airline code for the stop.")
    //     private String iataCode;

    //     @JsonPropertyDescription("Stop duration in ISO8601 format.")
    //     private String duration;

    //     @JsonPropertyDescription("Arrival time at the stop in ISO8601 format.")
    //     private String arrivalAt;

    //     @JsonPropertyDescription("Departure time from the stop in ISO8601 format.")
    //     private String departureAt;
    // }

    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Price {
        // @JsonPropertyDescription("Price margin percentage (plus or minus) for booking.")
        // private String margin;

        @JsonPropertyDescription("Total amount paid by the user (including fees and selected additional services).")
        private String grandTotal;

        @JsonPropertyDescription("Currency of the payment.")
        private String billingCurrency;

        // @JsonPropertyDescription("List of additional services.")
        // private List<AdditionalService> additionalServices;

        @JsonPropertyDescription("Currency of the price.")
        private String currency;

        @JsonPropertyDescription("Total amount paid by the user.")
        private String total;

        @JsonPropertyDescription("Amount without taxes.")
        private String base;

        // @JsonPropertyDescription("List of applicable fees.")
        // private List<Fee> fees;

        // @JsonPropertyDescription("List of applicable taxes.")
        // private List<Tax> taxes;

        // @JsonPropertyDescription("Amount of taxes which are refundable.")
        // private String refundableTaxes;
    }

    // @Data
    // @JsonInclude(JsonInclude.Include.NON_NULL)
    // @JsonIgnoreProperties(ignoreUnknown = true)
    // public static class AdditionalService {
    //     @JsonPropertyDescription("Amount for the additional service.")
    //     private String amount;

    //     @JsonPropertyDescription("Type of the additional service (CHECKED_BAGS, MEALS, SEATS, OTHER_SERVICES).")
    //     private String type;
    // }

    // @Data
    // @JsonInclude(JsonInclude.Include.NON_NULL)
    // @JsonIgnoreProperties(ignoreUnknown = true)
    // public static class Fee {
    //     @JsonPropertyDescription("Amount for the fee.")
    //     private String amount;

    //     @JsonPropertyDescription("Type of the fee (TICKETING, FORM_OF_PAYMENT, SUPPLIER).")
    //     private String type;
    // }

    // @Data
    // @JsonInclude(JsonInclude.Include.NON_NULL)
    // @JsonIgnoreProperties(ignoreUnknown = true)
    // public static class Tax {
    //     @JsonPropertyDescription("Amount for the tax.")
    //     private String amount;

    //     @JsonPropertyDescription("Code for the tax.")
    //     private String code;
    // }

    // @Data
    // @JsonInclude(JsonInclude.Include.NON_NULL)
    // @JsonIgnoreProperties(ignoreUnknown = true)
    // public static class PricingOptions {
    //     @JsonPropertyDescription("List of fare types (PUBLISHED, NEGOTIATED, CORPORATE).")
    //     private List<String> fareType;

    //     @JsonPropertyDescription("If true, returns the flight-offers with included checked bags only.")
    //     private boolean includedCheckedBagsOnly;

    //     @JsonPropertyDescription("If true, returns the flight-offers with refundable fares only.")
    //     private boolean refundableFare;

    //     @JsonPropertyDescription("If true, returns the flight-offers with no restriction fares only.")
    //     private boolean noRestrictionFare;

    //     @JsonPropertyDescription("If true, returns the flight-offers with no penalty fares only.")
    //     private boolean noPenaltyFare;
    // }

    // @Data
    // @JsonInclude(JsonInclude.Include.NON_NULL)
    // @JsonIgnoreProperties(ignoreUnknown = true)
    // public static class TravelerPricing {
    //     @JsonPropertyDescription("ID of the traveler.")
    //     private String travelerId;

    //     @JsonPropertyDescription("Fare option for the traveler (STANDARD, INCLUSIVE_TOUR, etc.).")
    //     private String fareOption;

    //     @JsonPropertyDescription("Type of the traveler (ADULT, CHILD, SENIOR, etc.).")
    //     private String travelerType;

    //     @JsonPropertyDescription("ID of the associated adult if type is HELD_INFANT.")
    //     private String associatedAdultId;

    //     @JsonPropertyDescription("Price information for the traveler.")
    //     private Price price;

    //     @JsonPropertyDescription("Fare details of the segment.")
    //     private List<FareDetailsBySegment> fareDetailsBySegment;
    // }

    // @Data
    // @JsonInclude(JsonInclude.Include.NON_NULL)
    // @JsonIgnoreProperties(ignoreUnknown = true)
    // public static class FareDetailsBySegment {
    //     @JsonPropertyDescription("ID of the segment.")
    //     private String segmentId;

    //     @JsonPropertyDescription("Quality of service offered in the cabin (ECONOMY, PREMIUM_ECONOMY, BUSINESS, FIRST).")
    //     private String cabin;

    //     @JsonPropertyDescription("Fare basis specifying the rules of a fare.")
    //     private String fareBasis;

    //     @JsonPropertyDescription("Name of the Fare Family corresponding to the fares.")
    //     private String brandedFare;

    //     @JsonPropertyDescription("Code of the booking class.")
    //     private String fareClass;

    //     @JsonPropertyDescription("If true, the corresponding booking class is in an allotment.")
    //     private boolean isAllotment;

    //     @JsonPropertyDescription("Details of allotment.")
    //     private AllotmentDetails allotmentDetails;

    //     @JsonPropertyDescription("Slice and Dice indicator.")
    //     private String sliceDiceIndicator;

    //     @JsonPropertyDescription("Baggage allowance information.")
    //     private BaggageAllowance includedCheckedBags;

    //     @JsonPropertyDescription("Details of chargeable additional services.")
    //     private AdditionalServices additionalServices;
    // }

    // @Data
    // @JsonInclude(JsonInclude.Include.NON_NULL)
    // @JsonIgnoreProperties(ignoreUnknown = true)
    // public static class AllotmentDetails {
    //     @JsonPropertyDescription("The tour name agreed for this specific allotment.")
    //     private String tourName;

    //     @JsonPropertyDescription("The tour reference agreed for this specific allotment.")
    //     private String tourReference;
    // }

    // @Data
    // @JsonInclude(JsonInclude.Include.NON_NULL)
    // @JsonIgnoreProperties(ignoreUnknown = true)
    // public static class BaggageAllowance {
    //     @JsonPropertyDescription("Total number of units.")
    //     private int quantity;

    //     @JsonPropertyDescription("Weight of the baggage allowance.")
    //     private int weight;

    //     @JsonPropertyDescription("Unit of weight (KG or LB).")
    //     private String weightUnit;
    // }

    // @Data
    // @JsonInclude(JsonInclude.Include.NON_NULL)
    // @JsonIgnoreProperties(ignoreUnknown = true)
    // public static class AdditionalServices {
    //     @JsonPropertyDescription("Details of chargeable checked bags.")
    //     private ChargeableCheckedBags chargeableCheckedBags;

    //     @JsonPropertyDescription("Details of chargeable seat.")
    //     private ChargeableSeat chargeableSeat;

    //     @JsonPropertyDescription("Other services to add.")
    //     private List<ServiceName> otherServices;
    // }

    // @Data
    // @JsonInclude(JsonInclude.Include.NON_NULL)
    // @JsonIgnoreProperties(ignoreUnknown = true)
    // public static class ChargeableCheckedBags {
    //     @JsonPropertyDescription("Total number of units.")
    //     private int quantity;

    //     @JsonPropertyDescription("Weight of the baggage allowance.")
    //     private int weight;

    //     @JsonPropertyDescription("Unit of weight (KG or LB).")
    //     private String weightUnit;

    //     @JsonPropertyDescription("ID of the chargeable bag.")
    //     private String id;
    // }

    // @Data
    // @JsonInclude(JsonInclude.Include.NON_NULL)
    // @JsonIgnoreProperties(ignoreUnknown = true)
    // public static class ChargeableSeat {
    //     @JsonPropertyDescription("ID of the chargeable seat.")
    //     private String id;

    //     @JsonPropertyDescription("Seat number.")
    //     private String number;
    // }

    // @Data
    // @JsonInclude(JsonInclude.Include.NON_NULL)
    // @JsonIgnoreProperties(ignoreUnknown = true)
    // public static class ServiceName {
    //     @JsonPropertyDescription("Type of service (PRIORITY_BOARDING, AIRPORT_CHECKIN).")
    //     private String type;
    // }

    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Dictionaries {
        @JsonPropertyDescription("Locations dictionary.")
        private LocationEntry locations;

        @JsonPropertyDescription("Aircraft dictionary.")
        private AircraftEntry aircraft;

        @JsonPropertyDescription("Currencies dictionary.")
        private CurrencyEntry currencies;

        @JsonPropertyDescription("Carriers dictionary.")
        private CarrierEntry carriers;
    }

    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class LocationEntry {
        @JsonPropertyDescription("City code associated to the airport.")
        private String cityCode;

        @JsonPropertyDescription("Country code of the airport.")
        private String countryCode;
    }

    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AircraftEntry {
        @JsonPropertyDescription("The manufacturer/model of aircraft.")
        private String manufacturerModel;
    }

    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CurrencyEntry {
        @JsonPropertyDescription("The currency code.")
        private String currencyCode;
    }

    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CarrierEntry {
        @JsonPropertyDescription("The carrier name.")
        private String carrierName;
    }
}
