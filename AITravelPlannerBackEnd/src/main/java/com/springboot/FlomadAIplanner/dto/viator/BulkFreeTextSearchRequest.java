package com.springboot.FlomadAIplanner.dto.viator;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BulkFreeTextSearchRequest {

    @NotBlank
    @JsonPropertyDescription("REQUIRED. NotBlank. Used to set a list of free-text search terms for products, attractions or destinations for which tours must be searched. Example: 'Eiffel Tower'")
    private List<String> searchTerms;

    @Valid
    @JsonPropertyDescription("Optional. Criteria by which to filter product search results. Optional field for detailed search filtering.")
    private FreeTextSearchRequest.ProductFiltering productFiltering;

    @Valid
    @JsonPropertyDescription("Optional. Specifies the sorting method for product results. Optional field for sorting search results.")
    private FreeTextSearchRequest.ProductSorting productSorting;

    @NotEmpty
    @Size(min = 1, max = 3)
    @Valid
    @JsonPropertyDescription("REQUIRED. Min 1, Max 3 items. Specifies the domain(s) in which to search for the search term and the respective pagination details.")
    private List<FreeTextSearchRequest.SearchType> searchTypes;

    @NotBlank
    @Pattern(regexp = "AUD|BRL|CAD|CHF|DKK|EUR|GBP|HKD|INR|JPY|NOK|NZD|SEK|SGD|TWD|USD|ZAR", message = "Invalid currency code")
    @JsonPropertyDescription("REQUIRED. regexp = \"AUD|BRL|CAD|CHF|DKK|EUR|GBP|HKD|INR|JPY|NOK|NZD|SEK|SGD|TWD|USD|ZAR\". Default is 'USD'. Currency code of price range in request filter and the prices in response. Must be a valid ISO 4217 code.")
    private String currency = "USD";

}
