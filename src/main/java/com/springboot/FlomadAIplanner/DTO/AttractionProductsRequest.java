package com.springboot.FlomadAIplanner.DTO;

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
    private int seoId;

    @Pattern(regexp = "\\d+-\\d+", message = "topX must be in the format {start}-{end}")
    private String topX;

    @Pattern(regexp = "SEO_PRODUCT_TOP_SELLERS|SEO_PRODUCT_REVIEW_AVG_RATING_A|SEO_PRODUCT_REVIEW_AVG_RATING_D|SEO_PRODUCT_PRICE_FROM_A|SEO_PRODUCT_PRICE_FROM_D", message = "sortOrder must be one of the specified values")
    private String sortOrder;

    private String currencyCode;
}