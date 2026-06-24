package com.coffeeshop.api.dto.adminDashboard;

import com.coffeeshop.api.dto.adminDashboard.product.ProductStockStatusResponse;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import java.util.List;
import java.util.UUID;

@Builder
public record TopSellingProductResponse(

        @JsonProperty("pagination")
        Pagination pagination,

        @JsonProperty("units_target")
        Integer unitsTarget,

        @JsonProperty("top_products")
        List<TopProductItem> topProducts
) {

    @Builder
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

    @Builder
    public record Pagination (
            @JsonProperty("page")
            Integer page,

            @JsonProperty("size")
            Integer size,

            @JsonProperty("total_pages")
            Integer totalPages,

            @JsonProperty("total_items")
            Long totalItems
    ) {}
}
