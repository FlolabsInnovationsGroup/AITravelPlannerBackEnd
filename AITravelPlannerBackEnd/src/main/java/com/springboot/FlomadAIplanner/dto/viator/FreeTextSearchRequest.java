package com.springboot.FlomadAIplanner.dto.viator;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FreeTextSearchRequest {

    @NotBlank
    @JsonPropertyDescription("REQUIRED. NotBlank. Used to set free-text search term. Example: 'Eiffel Tower'")
    private String searchTerm;

    @Valid
    @JsonPropertyDescription("Optional. Criteria by which to filter product search results. Optional field for detailed search filtering.")
    private ProductFiltering productFiltering;

    @Valid
    @JsonPropertyDescription("Optional. Specifies the sorting method for product results. Optional field for sorting search results.")
    private ProductSorting productSorting;

    @NotEmpty
    @Size(min = 1, max = 3)
    @Valid
    @JsonPropertyDescription("Required. Min 1, Max 3 items. Specifies the domain(s) in which to search for the search term and the respective pagination details.")
    private List<SearchType> searchTypes;

    @NotBlank
    @Pattern(regexp = "AUD|BRL|CAD|CHF|DKK|EUR|GBP|HKD|INR|JPY|NOK|NZD|SEK|SGD|TWD|USD|ZAR", message = "Invalid currency code")
    @JsonPropertyDescription("Required. Currency code of price range in request filter and the prices in response. Must be a valid ISO 4217 code.")
    private String currency;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductFiltering {

        @Pattern(regexp = "\\d+", message = "Destination must be a valid Viator destination integer ID")
        @JsonPropertyDescription("Optional. regexp = \"\\\\d+\". Match results that are assigned this destination. Only Viator destination integer ID is accepted.")
        private Integer destination;

        @Valid
        @JsonPropertyDescription("Optional. Match products that are available to be booked within this date range.")
        private DateRange dateRange;

        @Valid
        @JsonPropertyDescription("Optional. Match products which have a 'from price' within the range indicated here.")
        private PriceRange price;

        @Valid
        @JsonPropertyDescription("Optional. Match products that have an average rating within the range indicated here.")
        private RatingRange rating;

        @Valid
        @JsonPropertyDescription("Optional. Match products that have a duration within the range indicated here.")
        private DurationRange durationInMinutes;

        @Size(max = 6)
        @JsonPropertyDescription("Optional. maxSize = 6. Match products that have the following attributes. Maximum 6 items. Allowed values: " +
                "'NEW_ON_VIATOR' – products added within the past 270 days, " +
                "'SKIP_THE_LINE' – products allowing participants to skip the line, " +
                "'PRIVATE_TOUR' – products where only the travelers who have booked will be present, " +
                "'SPECIAL_OFFER' – products with a pricing discount available, " +
                "'LIKELY_TO_SELL_OUT' – popular products that routinely sell out.")
        private List<@NotBlank @Pattern(regexp = "NEW_ON_VIATOR|SKIP_THE_LINE|PRIVATE_TOUR|SPECIAL_OFFER|LIKELY_TO_SELL_OUT", message = "Invalid flag value") String> flags = Collections.emptyList();

        @JsonPropertyDescription("Optional. Specifies whether products with automatically machine-translated content should be included in the results.")
        private Boolean includeAutomaticTranslations = true;

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        public static class DateRange {
            @NotBlank
            @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}", message = "Date must be in YYYY-MM-DD format")
            @JsonPropertyDescription("Optional (But required if 'to' date is present. Should be the next date from current date by default). Match results that are available from this date. Format: YYYY-MM-DD")
            private String from;

            @NotBlank
            @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}", message = "Date must be in YYYY-MM-DD format")
            @JsonPropertyDescription("Optional (But required if 'from' date is present. Should be 2 weeks after the from date by default). Match results that are available until this date. Format: YYYY-MM-DD")
            private String to;
        }

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        public static class PriceRange {
            @DecimalMin("0.0")
            @JsonPropertyDescription("Optional. Min value is 0.0. Match results that have a price within this range.")
            private Double from;

            @DecimalMin("0.01")
            @JsonPropertyDescription("Optional. Min value is 0.01. Match results that have a price within this range.")
            private Double to;
        }

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        public static class RatingRange {
            @DecimalMin("0.0")
            @JsonPropertyDescription("Optional. Min value is 0.0. Match results that have an average rating within this range.")
            private Double from;

            @DecimalMin("0.01")
            @JsonPropertyDescription("Optional. Min value is 0.01. Match results that have an average rating within this range.")
            private Double to;
        }

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        public static class DurationRange {
            @Min(0)
            @JsonPropertyDescription("Optional. Min value is 0. Match results that have a duration within this range.")
            private Integer from;

            @Min(1)
            @JsonPropertyDescription("Optional. Min value is 1. Match results that have a duration within this range.")
            private Integer to;
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductSorting {
        @Pattern(regexp = "DEFAULT|ITINERARY_DURATION|PRICE|REVIEW_AVG_RATING|DATE_ADDED", message = "Invalid sort method")
        @JsonPropertyDescription("Optional. regexp = DEFAULT|ITINERARY_DURATION|PRICE|REVIEW_AVG_RATING|DATE_ADDED. The method of sorting results.")
        private String sort;

        @Pattern(regexp = "ASCENDING|DESCENDING", message = "Invalid order direction")
        @JsonPropertyDescription("Optional. regexp = ASCENDING|DESCENDING.")
        private String order;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SearchType {
        @NotBlank
        @Pattern(regexp = "ATTRACTIONS|DESTINATIONS|PRODUCTS", message = "Invalid search type")
        @JsonPropertyDescription("Required. regexp = ATTRACTIONS|DESTINATIONS|PRODUCTS. Specifies a domain within which the search should be performed.")
        private String searchType;

        @Valid
        @JsonPropertyDescription("Optional. Pagination details specifying which search results to return based on start position and item count.")
        private Pagination pagination = new Pagination();

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        public static class Pagination {
            @Min(1)
            @JsonPropertyDescription("Required. Min value is 1. Default is 1. Specifies the start position of the search results to return.")
            private Integer start = 1;

            @Min(1)
            @Max(100)
            @JsonPropertyDescription("Required. Min value is 1. Max is 100. Default is 5. Specifies the number of search results to return.")
            private Integer count = 5;
        }
    }
}
