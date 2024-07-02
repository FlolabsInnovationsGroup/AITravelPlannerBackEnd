package com.springboot.FlomadAIplanner.dto.viator;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AttractionProductsRequest {

    @NotNull(message = "seoId is required")
    @Positive(message = "seoId must be a positive integer")
    @JsonPropertyDescription("Required. Positive integer. Unique numeric identifier for the attraction to retrieve recommendation details for. Example: 14235.")
    private int seoId;

    @Pattern(regexp = "\\d+-\\d+", message = "topX must be in the format {start}-{end}")
    @JsonPropertyDescription("Optional. String in the format {start}-{end}. Specifies the start and end rows to return. Example: '1-10', '11-20'. The maximum number of rows per request is 100; if not specified, the default is '1-100'.")
    private String topX;

    @Pattern(regexp = "SEO_PRODUCT_TOP_SELLERS|SEO_PRODUCT_REVIEW_AVG_RATING_A|SEO_PRODUCT_REVIEW_AVG_RATING_D|SEO_PRODUCT_PRICE_FROM_A|SEO_PRODUCT_PRICE_FROM_D", message = "sortOrder must be one of the specified values")
    @JsonPropertyDescription("Optional. Specifies the sort order in which to return results. Allowed values: " +
            "'SEO_PRODUCT_TOP_SELLERS' – Top Sellers, " +
            "'SEO_PRODUCT_REVIEW_AVG_RATING_A' – Traveller Rating (low->high), " +
            "'SEO_PRODUCT_REVIEW_AVG_RATING_D' – Traveller Rating (high->low), " +
            "'SEO_PRODUCT_PRICE_FROM_A' – Price (low->high), " +
            "'SEO_PRODUCT_PRICE_FROM_D' – Price (high->low).")
    private String sortOrder;

    @JsonPropertyDescription("Optional. Currency code for displaying pricing details for the products associated with the specified attraction. Example: EUR. You must have multiple currencies enabled to use this parameter.")
    private String currencyCode;
}
