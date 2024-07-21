package com.springboot.FlomadAIplanner.dto.viator;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.springboot.FlomadAIplanner.util.ToStringUtil;
import lombok.*;

import java.util.List;

@Getter
@Setter
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FreeTextSearchResponse {

    @JsonPropertyDescription("Destinations that include the searchTerm when 'DESTINATIONS' results are requested via searchTypes.")
    private Destinations destinations;

    @JsonPropertyDescription("Attractions that include the searchTerm when 'ATTRACTIONS' results are requested via searchTypes.")
    private Attractions attractions;

    @JsonPropertyDescription("Products that include the searchTerm when 'PRODUCTS' results are requested via searchTypes.")
    private Products products;

    @JsonPropertyDescription("Error message if the search request fails.")
    private String errorMessage;

    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Destinations {
        @JsonPropertyDescription("Total number of destinations that may be returned if pagination is not specified in the request.")
        private long totalCount;

        @JsonPropertyDescription("List of matching destinations.")
        private List<DestinationSearchResult> results;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class DestinationSearchResult {
        @JsonPropertyDescription("Unique numeric identifier of the destination.")
        private long id;

        @JsonPropertyDescription("Natural-language name of the destination.")
        private String name;

        @JsonPropertyDescription("Unique numeric identifier of the destination's parent destination.")
        private long parentDestinationId;

        @JsonPropertyDescription("Natural-language name of the destination's parent destination.")
        private String parentDestinationName;

//        @JsonPropertyDescription("Information about whether the text in this response was machine-translated.")
//        private TranslationDetails translationInfo;

        @JsonPropertyDescription("URL of destination.")
        private String url;
    }

    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Attractions {
        @JsonPropertyDescription("Total number of attractions that may be returned if pagination is not specified in the request.")
        private long totalCount;

        @JsonPropertyDescription("List of matching attractions.")
        private List<AttractionResult> results;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class AttractionResult {
        @JsonPropertyDescription("Unique numeric identifier of the attraction.")
        private long id;

        @JsonPropertyDescription("Natural-language title of the attraction.")
        private String name;

        @JsonPropertyDescription("Unique numeric identifier of this attraction's primary destination.")
        private long primaryDestinationId;

//        @JsonPropertyDescription("Natural-language descriptive text for this attraction.")
//        private String description;

        @JsonPropertyDescription("Natural-language name of the primary destination associated with this attraction.")
        private String destinationName;

        @JsonPropertyDescription("Number of products associated with this attraction.")
        private int productsCount;

//        @JsonPropertyDescription("Information about whether the text in this response was machine-translated.")
//        private TranslationDetails translationInfo;

        @JsonPropertyDescription("Summary of reviews for this attraction.")
        private ProductReviewsSummary reviews;
    }

    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Products {
        @JsonPropertyDescription("Total number of products that may be returned if pagination is not specified in the request.")
        private long totalCount;

        @JsonPropertyDescription("List of matching products.")
        private List<ProductSummary> results;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ProductSummary {
        @JsonPropertyDescription("Code that is the unique identifier for this product.")
        private String productCode;

        @JsonPropertyDescription("Natural-language title of this product.")
        private String title;

//        @JsonPropertyDescription("Description of this product.")
//        private String description;

//        @JsonPropertyDescription("Images for this product.")
//        private List<Image> images;

        @JsonPropertyDescription("Summary of reviews and ratings for this product.")
        private ProductReviews reviews;

        @JsonPropertyDescription("Duration information for this itinerary.")
        private ItineraryDuration duration;

        @JsonPropertyDescription("High-level summary of product prices.")
        private ProductSearchPricing pricing;

        @JsonPropertyDescription("Destinations in which this product operates.")
        private List<Destination> destinations;

//        @JsonPropertyDescription("List of product attribute flags specified in the request.")
//        private List<String> flags;

//        @JsonPropertyDescription("Machine-interpretable value encoding when bookings for this product will be confirmed.")
//        private String confirmationType;

//        @JsonPropertyDescription("Machine-interpretable value specifying the type of itinerary available for this product.")
//        private String itineraryType;

//        @JsonPropertyDescription("Information about whether the text in this response was machine-translated.")
//        private TranslationDetails translationInfo;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Image {
        @JsonPropertyDescription("URL of the image.")
        private String url;

        @JsonPropertyDescription("Description of the image.")
        private String description;

        @JsonPropertyDescription("True if this photo is considered to be the cover (leading) photo for this product from this imageSource.")
        private boolean isCover;

        @JsonPropertyDescription("Dimension/resolution variants available for this image.")
        private List<ImageVariant> variants;

        @JsonPropertyDescription("Machine-interpretable value indicating the source of this image; one of: 'SUPPLIER_PROVIDED', 'PROFESSIONAL'.")
        private String imageSource;

        @JsonPropertyDescription("Caption for this image.")
        private String caption;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ImageVariant {
        @JsonPropertyDescription("Height of this image in pixels.")
        private int height;

        @JsonPropertyDescription("Width of this image in pixels.")
        private int width;

        @JsonPropertyDescription("URL from which this image can be retrieved.")
        private String url;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ItineraryDuration {
        @JsonPropertyDescription("Duration of this itinerary (in minutes) in the case that it takes a fixed amount of time.")
        private int fixedDurationInMinutes;

        @JsonPropertyDescription("Lower limit of the duration of this itinerary (in minutes) in the case that this product's duration varies.")
        private int variableDurationFromMinutes;

        @JsonPropertyDescription("Upper limit of the duration of this itinerary (in minutes) in the case that this product's duration varies.")
        private int variableDurationToMinutes;

        @JsonPropertyDescription("Unstructured duration information.")
        private String unstructuredDuration;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ProductSearchPricing {
        @JsonPropertyDescription("Currency code for the currency in which the prices in summary are denominated.")
        private String currency;

        @JsonPropertyDescription("Lowest per-person price for this product expressed as the amount (excluding the booking fee) that merchant partners using the markup/booking-fee pricing scheme would be invoiced.")
        private double partnerNetFromPrice;

        @JsonPropertyDescription("URL for this product on the viator.com site, where the customer will complete their booking.")
        private String productUrl;

        @JsonPropertyDescription("Summary of pricing information for this product.")
        private Summary summary;

        @Data
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Summary {
            @JsonPropertyDescription("If the product is presently discounted, this field shows what the value of fromPrice would be if no discount had been applied; otherwise, it will be equal to fromPrice.")
            private double fromPriceBeforeDiscount;

            @JsonPropertyDescription("Lowest per-person retail price for this product.")
            private double fromPrice;
        }
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Destination {
        @JsonPropertyDescription("Unique numeric identifier for this destination.")
        private String ref;

        @JsonPropertyDescription("True if this is the primary destination associated with this product.")
        private boolean primary;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ProductReviewsSummary {
//        @JsonPropertyDescription("Breakdown of review summaries for each review source.")
//        private List<ProductReviewsSummarySource> sources;

//        @JsonPropertyDescription("Breakdown of number of reviews per rating for all reviews of this product.")
//        private List<ProductReviewsSummaryCount> reviewCountTotals;

        @JsonPropertyDescription("Total number of reviews available for this product from all sources.")
        private long totalReviews;

        @JsonPropertyDescription("Combined average rating for this product across all reviews from all sources.")
        private float combinedAverageRating;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ProductReviewsSummarySource {
        @JsonPropertyDescription("Provider for this set of reviews; one of: 'VIATOR', 'TRIPADVISOR'.")
        private String provider;

        @JsonPropertyDescription("Breakdown of number of reviews per rating for reviews sourced from this provider.")
        private List<ProductReviewsSummaryCount> reviewCounts;

        @JsonPropertyDescription("Total number of reviews for this product sourced from this provider.")
        private long totalCount;
    }

    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ProductReviewsSummaryCount {
        @JsonPropertyDescription("This object refers to reviews with this star-rating.")
        private float rating;

        @JsonPropertyDescription("Number of reviews for this product with a star-rating of rating.")
        private long count;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ProductReviews {
//        @JsonPropertyDescription("Breakdown of ratings, counts, and the sources of the reviews of this product.")
//        private List<ProductReviewSource> sources;
//
//        @JsonPropertyDescription("Combined total number of reviews per rating across all sources for this product.")
//        private List<ProductReviewCount> reviewCountTotals;

        @JsonPropertyDescription("Total number of reviews from all sources for this product.")
        private long totalReviews;

        @JsonPropertyDescription("Average rating for all reviews from all sources for this product.")
        private float combinedAverageRating;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ProductReviewSource {
        @JsonPropertyDescription("Source of the review; one of: 'VIATOR', 'TRIPADVISOR'.")
        private String provider;

        @JsonPropertyDescription("Breakdown of ratings and counts for reviews from this source for this product.")
        private List<ProductReviewCount> reviewCounts;

        @JsonPropertyDescription("Total number of reviews for this product sourced from this provider.")
        private long totalCount;

        @JsonPropertyDescription("Average rating across all reviews from this source for this product.")
        private float averageRating;
    }

    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ProductReviewCount {
        @JsonPropertyDescription("This object refers to reviews with this star-rating.")
        private float rating;

        @JsonPropertyDescription("Number of reviews for this product with this rating.")
        private long count;

        @JsonPropertyDescription("Total number of reviews for all ratings of this product.")
        private long totalCount;

        @JsonPropertyDescription("Average rating across all reviews from this source for this product.")
        private float averageRating;
    }

    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class TranslationDetails {
        @JsonPropertyDescription("Indicates whether this product description utilizes text in any of its natural language fields that was automatically machine-translated with no human oversight.")
        private boolean containsMachineTranslatedText;

        @JsonPropertyDescription("Source of the translation.")
        private String translationSource;

        @JsonPropertyDescription("Value specifying the machine-translation service vendor.")
        private String translationAttribution;
    }

    @Override
    public String toString() {
        return ToStringUtil.toStringNonNullOnly(this);
    }

}
