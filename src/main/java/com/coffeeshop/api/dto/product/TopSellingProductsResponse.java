package com.coffeeshop.api.dto.product;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import java.util.List;
import java.util.UUID;

@Builder
public record TopSellingProductsResponse(
        @JsonProperty("units_target")
        Integer unitsTarget,

        @JsonProperty("top_products")
        List<TopProducts> topProducts
) {
    public record TopProducts(
            @JsonProperty("product_id")
            UUID productId,

            @JsonProperty("product_name")
            String productName,

            @JsonProperty("image_url")
            String imageUrl,

            @JsonProperty("units_sold")
            Integer unitsSold
    ){}
}
