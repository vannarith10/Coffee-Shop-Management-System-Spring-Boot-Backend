package com.coffeeshop.api.dto.adminDashboard;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import java.util.List;
import java.util.UUID;

@Builder
public record TopSellingProductResponse(
        @JsonProperty("units_target")
        Integer unitsTarget,

        @JsonProperty("top_products")
        List<TopProductItem> topProducts
) {
    public record TopProductItem(
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
